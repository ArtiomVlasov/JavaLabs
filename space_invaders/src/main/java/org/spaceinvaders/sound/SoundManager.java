package org.spaceinvaders.sound;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, byte[]> soundData;
    private Set<Clip> activeClips;
    private boolean isMuted = false;
    private ExecutorService soundExecutor;
    private ScheduledExecutorService cleanupExecutor;
    private long lastAlienSoundTime = 0;
    private static final long ALIEN_SOUND_COOLDOWN = 300;
    private static final int MAX_CONCURRENT_SOUNDS = 32;

    private SoundManager() {
        soundData = new HashMap<>();
        activeClips = Collections.synchronizedSet(new HashSet<>());
        
        soundExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "SoundThread");
            return t;
        });
        
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SoundCleanupThread");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        
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
            try{

                CompletableFuture.allOf(
                    CompletableFuture.runAsync(() -> loadSoundEffect("shoot", "shoot.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invaderKilled", "invaderkilled.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invader1", "invader1.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invader2", "invader2.wav")),
                    CompletableFuture.runAsync(() -> loadSoundEffect("invader3", "invader3.wav"))
                ).join();
                
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
            }
        } catch (Exception e) {
            System.err.println("Error loading sound effect " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] loadSoundData(String resourcePath) {
        try {
            InputStream audioStream = getClass().getResourceAsStream(resourcePath);
            
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            
            while ((nRead = audioStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            buffer.flush();
            audioStream.close();
            
            return buffer.toByteArray();
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
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                            new ByteArrayInputStream(data)
                        );

                        Clip clip = AudioSystem.getClip();
                        clip.open(audioStream);
                        setClipVolume(clip, volume);
                        activeClips.add(clip);
                        clip.start();

                        clip.addLineListener(event -> {
                            if (event.getType() == LineEvent.Type.STOP) {
                                activeClips.remove(clip);
                                clip.close();
                                try {
                                    audioStream.close();
                                } catch (IOException e) {
                                    System.err.println("Error closing stream: " + e.getMessage());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error playing sound " + soundName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }, soundExecutor);
        }
    }

    public void playSound(String soundName) {
        if (isMuted) return;
        float volume = 0.6f;
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
        
        cleanupExecutor.scheduleAtFixedRate(this::cleanupFinishedClips, 1, 1, TimeUnit.SECONDS);
    }

    public void reset() {
        synchronized (activeClips) {
            for (Clip clip : activeClips) {
                clip.stop();
                clip.close();
            }
            activeClips.clear();
        }

        resetExecutors();
        
        isMuted = false;
        lastAlienSoundTime = 0;

        loadSounds();
    }

    public void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            synchronized (activeClips) {
                for (Clip clip : activeClips) {
                    clip.stop();
                }
            }
        }
    }

    public boolean isMuted() {
        return isMuted;
    }
} 