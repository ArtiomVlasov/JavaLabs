package org.spaceinvaders.sound;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, byte[]> soundData;
    private Set<Clip> activeClips;
    private Clip backgroundMusic;
    private boolean isMuted = false;
    private ExecutorService soundExecutor;
    private ScheduledExecutorService cleanupExecutor;
    private long lastAlienSoundTime = 0;
    private static final long ALIEN_SOUND_COOLDOWN = 300; // 300ms cooldown
    private static final int MAX_CONCURRENT_SOUNDS = 32;

    private SoundManager() {
        soundData = new HashMap<>();
        activeClips = Collections.synchronizedSet(new HashSet<>());
        
        // Single thread pool for sound playback
        soundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "SoundThread");
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        });
        
        // Cleanup executor for removing finished clips
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SoundCleanupThread");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        
        // Schedule periodic cleanup of finished clips
        cleanupExecutor.scheduleAtFixedRate(this::cleanupFinishedClips, 1, 1, TimeUnit.SECONDS);
        
        loadSounds();
    }

    private void cleanupFinishedClips() {
        try {
            synchronized (activeClips) {
                Iterator<Clip> iterator = activeClips.iterator();
                while (iterator.hasNext()) {
                    Clip clip = iterator.next();
                    if (!clip.isRunning()) {
                        clip.close();
                        iterator.remove();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during clip cleanup: " + e.getMessage());
        }
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        CompletableFuture.runAsync(() -> {
            try {
                // Load background music
                try {
                    byte[] musicData = loadSoundData("/music/main_theme.wav");
                    if (musicData != null) {
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                            new ByteArrayInputStream(musicData)
                        );
                        backgroundMusic = AudioSystem.getClip();
                        backgroundMusic.open(audioStream);
                        setClipVolume(backgroundMusic, 0.2f); // 20% volume for background music
                        System.out.println("Background music loaded successfully");
                    }
                } catch (Exception e) {
                    System.err.println("Error loading background music: " + e.getMessage());
                    e.printStackTrace();
                }

                // Load sound effects concurrently
                CompletableFuture.allOf(
                    CompletableFuture.runAsync(() -> loadSoundEffect("shoot", "shoot.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invaderKilled", "invaderkilled.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invader1", "fastinvader1.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invader2", "fastinvader2.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invader3", "fastinvader3.wav"))
                ).join();
                
                System.out.println("All sound effects loaded successfully");
                
            } catch (Exception e) {
                System.err.println("Error in loadSounds: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void loadSoundEffect(String name, String filename) {
        try {
            byte[] soundData = loadSoundData("/music/" + filename);
            if (soundData != null) {
                this.soundData.put(name, soundData);
                System.out.println("Loaded sound effect: " + name);
            }
        } catch (Exception e) {
            System.err.println("Error loading sound effect " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] loadSoundData(String resourcePath) {
        try {
            // Try loading from resources first
            InputStream audioStream = getClass().getResourceAsStream(resourcePath);
            
            // If not found in resources, try file system
            if (audioStream == null) {
                String filePath = "src/main/resources" + resourcePath;
                File file = new File(filePath);
                if (file.exists()) {
                    audioStream = new FileInputStream(file);
                    System.out.println("Loading sound from file: " + filePath);
                } else {
                    System.err.println("Sound file not found: " + resourcePath);
                    return null;
                }
            } else {
                System.out.println("Loading sound from resources: " + resourcePath);
            }
            
            // Read the entire file into a byte array
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            
            while ((nRead = audioStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            buffer.flush();
            audioStream.close();
            
            byte[] soundData = buffer.toByteArray();
            System.out.println("Loaded " + soundData.length + " bytes for " + resourcePath);
            return soundData;
        } catch (Exception e) {
            System.err.println("Error loading sound data from " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }
        } catch (Exception e) {
            System.err.println("Error setting volume: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playSoundClip(String soundName, float volume) {
        if (!isMuted && activeClips.size() < MAX_CONCURRENT_SOUNDS) {
            CompletableFuture.runAsync(() -> {
                try {
                    byte[] data = soundData.get(soundName);
                    if (data != null) {
                        // Create a new audio stream and clip for each playback
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                            new ByteArrayInputStream(data)
                        );
                        
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioStream);
                        setClipVolume(clip, volume);
                        
                        // Add to active clips before starting
                        synchronized (activeClips) {
                            activeClips.add(clip);
                        }
                        
                        clip.start();
                        
                        // Add listener to handle cleanup
                        clip.addLineListener(event -> {
                            if (event.getType() == LineEvent.Type.STOP) {
                                synchronized (activeClips) {
                                    activeClips.remove(clip);
                                }
                                clip.close();
                                try {
                                    audioStream.close();
                                } catch (IOException e) {
                                    System.err.println("Error closing stream: " + e.getMessage());
                                }
                            }
                        });
                        
                        System.out.println("Playing sound: " + soundName + " (Active sounds: " + activeClips.size() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("Error playing sound " + soundName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }, soundExecutor);
        }
    }

    public void playBackgroundMusic() {
        if (backgroundMusic != null && !isMuted) {
            CompletableFuture.runAsync(() -> {
                try {
                    if (backgroundMusic.isRunning()) {
                        backgroundMusic.stop();
                    }
                    backgroundMusic.setFramePosition(0);
                    backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                    System.out.println("Started background music");
                } catch (Exception e) {
                    System.err.println("Error playing background music: " + e.getMessage());
                    e.printStackTrace();
                }
            }, soundExecutor);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    backgroundMusic.stop();
                    System.out.println("Stopped background music");
                } catch (Exception e) {
                    System.err.println("Error stopping background music: " + e.getMessage());
                    e.printStackTrace();
                }
            }, soundExecutor);
        }
    }

    public void playSound(String soundName) {
        if (isMuted) return;
        float volume = 0.6f; // Default volume for shooting
        if (soundName.equals("invaderKilled")) {
            volume = 0.7f;
        }
        playSoundClip(soundName, volume);
    }

    public void playInvaderSound(int type) {
        if (isMuted) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAlienSoundTime < ALIEN_SOUND_COOLDOWN) {
            return;
        }
        
        String soundName = "invader" + Math.min(type + 1, 3);
        playSoundClip(soundName, 0.5f);
        lastAlienSoundTime = System.currentTimeMillis();
    }

    private void resetExecutors() {
        // Shutdown existing executors
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
            }
        }
        
        if (soundExecutor != null) {
            soundExecutor.shutdown();
            try {
                if (!soundExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    soundExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                soundExecutor.shutdownNow();
            }
        }
        
        // Create new executors
        soundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "SoundThread");
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        });
        
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SoundCleanupThread");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        
        // Reschedule cleanup task
        cleanupExecutor.scheduleAtFixedRate(this::cleanupFinishedClips, 1, 1, TimeUnit.SECONDS);
    }

    public void reset() {
        // Stop all sounds first
        stopBackgroundMusic();
        synchronized (activeClips) {
            for (Clip clip : activeClips) {
                clip.stop();
                clip.close();
            }
            activeClips.clear();
        }
        
        // Reset executors
        resetExecutors();
        
        // Reset other state
        isMuted = false;
        lastAlienSoundTime = 0;
        
        // Reload sounds
        loadSounds();
    }

    public void cleanup() {
        try {
            // Stop and close all active clips
            synchronized (activeClips) {
                for (Clip clip : activeClips) {
                    try {
                        clip.stop();
                        clip.close();
                    } catch (Exception e) {
                        System.err.println("Error closing clip: " + e.getMessage());
                    }
                }
                activeClips.clear();
            }
            
            // Stop and close background music
            if (backgroundMusic != null) {
                backgroundMusic.stop();
                backgroundMusic.close();
            }
            
            // Shutdown executors
            if (cleanupExecutor != null) {
                cleanupExecutor.shutdown();
                if (!cleanupExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            }
            
            if (soundExecutor != null) {
                soundExecutor.shutdown();
                if (!soundExecutor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    soundExecutor.shutdownNow();
                }
            }
            
            // Clear instance
            instance = null;
            
            System.out.println("Sound system cleaned up");
            
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            stopBackgroundMusic();
            // Stop all active sounds
            synchronized (activeClips) {
                for (Clip clip : activeClips) {
                    clip.stop();
                }
            }
        } else {
            playBackgroundMusic();
        }
        System.out.println("Sound " + (isMuted ? "muted" : "unmuted"));
    }

    public boolean isMuted() {
        return isMuted;
    }
} 