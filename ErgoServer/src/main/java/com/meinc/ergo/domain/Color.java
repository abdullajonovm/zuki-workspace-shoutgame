package com.meinc.ergo.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.meinc.ergo.util.ColorSerializer;

//exchange category colors:
//  http://www.infinitec.de/image.axd?picture=WindowsLiveWriter/WorkingwiththeMasterCategoryListViaWebDA_C389/image_10.png
//  http://social.technet.microsoft.com/Forums/da-DK/exchangesvrdevelopmentlegacy/thread/e5c5f072-0b5c-49ce-9db7-57f76f5e011e
@JsonSerialize(using=ColorSerializer.class)
public enum Color
{
    NONE(       -1, "None",         "#ffffff"),
    RED(         0, "Red",          "#e4636a"),
    ORANGE(      1, "Orange",       "#f49454"),
    PEACH(       2, "Peach",        "#ffca4c"),
    YELLOW(      3, "Yellow",       "#fffe3d"),
    GREEN(       4, "Green",        "#83d17a"),
    TEAL(        5, "Teal",         "#7bd2b5"),
    OLIVE(       6, "Olive",        "#b0c18a"),
    BLUE(        7, "Blue",         "#729bd9"),
    PURPLE(      8, "Purple",       "#9278d1"),
    MAROON(      9, "Maroon",       "#c382a2"),
    STEEL(      10, "Steel",        "#c4ccdd"),
    DARKSTEEL(  11, "DarkSteel",    "#4b5978"),
    GRAY(       12, "Gray",         "#a8a8a8"),
    DARKGRAY(   13, "DarkGray",     "#525252"),
    BLACK(      14, "Black",        "#000000"),
    DARKRED(    15, "DarkRed",      "#ab1d24"),
    DARKORANGE( 16, "DarkOrange",   "#b14f0d"),
    DARKPEACH(  17, "DarkPeach",    "#ab7b05"),
    DARKYELLOW( 18, "DarkYellow",   "#999400"),
    DARKGREEN(  19, "DarkGreen",    "#35792b"),
    DARKTEAL(   20, "DarkTeal",     "#2e7d63"),
    DARKOLIVE(  21, "DarkOlive",    "#5f6c3a"),
    DARKBLUE(   22, "DarkBlue",     "#2a5191"),
    DARKPURPLE( 23, "DarkPurple",   "#50328f"),
    DARKMAROON( 24, "DarkMaroon",   "#82375f");
    
    private int color;
    private String name;
    private String hex;
    
    Color(int color, String name, String hex)
    {
        this.color = color;
        this.name = name;
        this.hex = hex;
    }
    
    public static Color fromColor(int color)
    {
        if (color < 0)
            return Color.NONE;
        else
            return Color.values()[color+1];
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getHex()
    {
        return hex;
    }

    public void setHex(String hex)
    {
        this.hex = hex;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("color: ").append(color);
        buf.append(", name: ").append(name);
        buf.append(", hex: ").append(hex);
        
        return buf.toString();
    }
    
//    public static void main(String[] args)
//    {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            
//            Color red = Color.RED;
//            mapper.writeValue(baos, red);
//            String output = baos.toString();
//            System.out.println("RED: " + output);
//            baos.reset();
//            
//            Color blue = Color.fromColor(7);
//            mapper.writeValue(baos, blue);
//            output = baos.toString();
//            System.out.println("BLUE: " + output);
//            
//            System.out.println("DONE");
//        } catch (Exception e) {
//            e.printStackTrace(System.err);
//        }
//    }

}
