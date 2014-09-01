package com.giannoules.proxstor.testing.stressor;

import com.giannoules.proxstor.api.Device;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DeviceGenerator {
    /*
     * list obtained from https://en.wikipedia.org/wiki/Category:Mobile_phone_manufacturers
     */
    static String[] manufacturerArray = {        
                        "Acer Inc.",
                        "AEG",
                        "Alcatel Mobile Phones",
                        "Amazon.com",
                        "Apple Inc.",
                        "Archos",
                        "Arise India",
                        "Asus",
                        "BenQ",
                        "Binatone",
                        "BlackBerry Limited",
                        "Brondi",
                        "Groupe Bull",
                        "Bullitt Group",
                        "BYD Electronic",
                        "Casio",
                        "CECT",
                        "Celkon Mobiles",
                        "Cherry Mobile",
                        "Danger (company)",
                        "DataWind",
                        "DBTel",
                        "Dell",
                        "Doro (telecoms)",
                        "Ericsson",
                        "Evertek",
                        "Fairphone",
                        "Firefly (mobile phone)",
                        "Foxconn",
                        "Fujitsu",
                        "G'Five",
                        "GeeksPhone",
                        "Twig Com",
                        "Gigabyte Technology",
                        "Gionee",
                        "Goldvish",
                        "Google",
                        "Gresso (company)",
                        "Grundig mobile",
                        "Handheld Group",
                        "HTC",
                        "Huawei",
                        "Huawei 4G eLTE",
                        "IBall (company)",
                        "IGB Eletrônica",
                        "InfoSonics Corporation",
                        "Intel",
                        "Inventec",
                        "Iriver",
                        "Jablotron",
                        "JCB (company)",
                        "John's Phone",
                        "Jolla",
                        "Just5",
                        "Karbonn Mobiles",
                        "Kejian",
                        "KT Tech EVER",
                        "Kyocera Communications",
                        "Kyoto Electronics",
                        "Lanix",
                        "Lenovo",
                        "LG Cyon",
                        "LG Electronics",
                        "Lumigon",
                        "M.Mobile",
                        "Meizu",
                        "Micromax Mobile",
                        "Microsoft Mobile",
                        "Mitsubishi Electric",
                        "MobiWire",
                        "Motorola Mobility",
                        "NEC",
                        "NEC Casio Mobile Communications",
                        "Neonode",
                        "Newkia",
                        "Nexian",
                        "Ningbo Bird",
                        "Nokia",
                        "Olivetti",
                        "OnePlus",
                        "Onida Electronics",
                        "Qi hardware",
                        "Oppo Electronics",
                        "Panasonic",
                        "Pantech",
                        "Pantech Wireless",
                        "Peiker Acustic",
                        "Samsung Telecommunications",
                        "Sanyo",
                        "Sharp Corporation",
                        "Siemens",
                        "Siemens Mexico",
                        "Sitronics",
                        "Sky Electronics",
                        "Sonim Technologies",
                        "Sony Mobile Communications",
                        "Spectronic",
                        "Starmobile",
                        "TCL Corporation",
                        "Technicolor SA",
                        "Technology Happy Life",
                        "Telit",
                        "Thuraya",
                        "Vertu",
                        "Verzo",
                        "Videocon",
                        "Vivo Electronics",
                        "Voxx International",
                        "Walton (company)",
                        "Walton Hi-Tech Industries Limited",
                        "Xiaomi",
                        "Yota",
                        "Zonda Telecom",
                        "Zopo Mobile",
                        "ZTE",
                        "Zync Global",
                    };
    
    static String[] modelArray = {
                        "Zoom",
                        "Super",
                        "Touch",
                        "Flip",
                        "Dance",
                        "Tablet",
                        "Andromeda",
                        "One",
                        "Two",
                        "Tetys",
                        "Helios",
                        "iCaller",
                        "Droidz",
                        "Droideka",
                        "View Port",
                        "Bent",
                        "Inverted",
                        "BlueBerry"
                     };
   
    static String[] osArray = {
                        "Cyborg Cupcake",
                        "Cyborg Donut",
                        "Cyborg Eclair",
                        "Cyborg Froyo",
                        "Cyborg Gingerbread",
                        "Cyborg Honeycomb",
                        "Cyborg Ice Cream Sandwich",
                        "Cyborg Jelly Bean",
                        "Cyborg KitKat",
                        "BoysenbBerry OS 1.0",
                        "BoysenbBerry OS 3.6",
                        "BoysenbBerry OS 5.0",
                        "BoysenbBerry OS 6.0",
                        "BoysenbBerry OS 7.0",
                        "BoysenbBerry OS 7.1",
                        "BoysenbBerry OS 10",
                        "uOS v3",
                        "uOS v4",
                        "uOS v5",
                        "uOS v6",
                        "uOS v7",
                        "uOS v8"
                    };

    private final List<String> manufacturers;
    private final List<String> models;
    private final List<String> operatingSystems;
    private final Random random;

    public DeviceGenerator() {
        manufacturers = Arrays.asList(manufacturerArray);
        models = Arrays.asList(modelArray);
        operatingSystems = Arrays.asList(osArray);
        random = new Random();
    }
        
    public Device genDevice() {
        Device d = new Device();
        d.setSerialNum(UUID.randomUUID().toString());
        d.setManufacturer(manufacturers.get(random.nextInt(manufacturers.size())));
        d.setModel(models.get(random.nextInt(models.size())));
        d.setOs(operatingSystems.get(random.nextInt(operatingSystems.size())));
        d.setDescription(d.getManufacturer() + " " + d.getModel() + " running " + d.getOs());        
        return d;
    }
}
