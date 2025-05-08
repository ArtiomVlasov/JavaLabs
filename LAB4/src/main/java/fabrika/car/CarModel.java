package fabrika.car;
import fabrika.CarPart.*;

import java.io.Serializable;

public class CarModel  implements Serializable {
    private static int counter = 0;
    private int id;
    private Engine engine;
    private BodyPart body;
    private Accessory accessory;

    public CarModel(Engine engine, BodyPart bodyPart, Accessory accessory){
        this.accessory = accessory;
        this.body = bodyPart;
        this.engine = engine;
        this.id = counter;
        counter++;
    }

    public Engine getEngine(){
        return engine;
    }

    public BodyPart  getBody(){
        return body;
    }

    public Accessory getAccessory(){
        return accessory;
    }

    public int getId() {
        return id;
    }
}
