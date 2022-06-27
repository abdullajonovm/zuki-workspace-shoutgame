package com.meinc.zztasks.provider.exchange;

import java.util.HashMap;
import java.util.Map;

public class ExchangeColorMapper
{
    private static Map<String,ErgoColor> hexToErgoColorMap = new HashMap<String,ErgoColor>();
    private static Map<String,ExchangeColor> hexToExchangeColorMap = new HashMap<String,ExchangeColor>();

    public enum ExchangeColor
    {
        NONE       (-1, "None",       null),
        RED        (0,  "Red",        "e4636a"),
        ORANGE     (1,  "Orange",     "f49454"),
        PEACH      (2,  "Peach",      "ffca4c"),
        YELLOW     (3,  "Yellow",     "fffe3d"),
        GREEN      (4,  "Green",      "83d17a"),
        TEAL       (5,  "Teal",       "7bd2b5"),
        OLIVE      (6,  "Olive",      "b0c18a"),
        BLUE       (7,  "Blue",       "729bd9"),
        PURPLE     (8,  "Purple",     "9278d1"),
        MAROON     (9,  "Maroon",     "c382a2"),
        STEEL      (10, "Steel",      "c4ccdd"),
        DARKSTEEL  (11, "DarkSteel",  "4b5978"),
        GRAY       (12, "Gray",       "a8a8a8"),
        DARKGRAY   (13, "DarkGray",   "525252"),
        BLACK      (14, "Black",      "000000"),
        DARKRED    (15, "DarkRed",    "ab1d24"),
        DARKORANGE (16, "DarkOrange", "b14f0d"),
        DARKPEACH  (17, "DarkPeach",  "ab7b05"),
        DARKYELLOW (18, "DarkYellow", "999400"),
        DARKGREEN  (19, "DarkGreen",  "35792b"),
        DARKTEAL   (20, "DarkTeal",   "2e7d63"),
        DARKOLIVE  (21, "DarkOlive",  "5f6c3a"),
        DARKBLUE   (22, "DarkBlue",   "2a5191"),
        DARKPURPLE (23, "DarkPurple", "50328f"),
        DARKMAROON (24, "DarkMaroon", "82375f");

        private int colorIndex;
        private String colorName;
        private String colorHex;
        
        ExchangeColor(int colorIndex, String colorName, String colorHex)
        {
            this.colorIndex = colorIndex;
            this.colorName = colorName;
            this.colorHex = colorHex;
            hexToExchangeColorMap.put(colorHex, this);
        }

        public static ExchangeColor fromColor(int colorIndex)
        {
            if (colorIndex < 0 || colorIndex >= ExchangeColor.values().length)
                return ExchangeColor.NONE;
            else
                return ExchangeColor.values()[colorIndex+1];
        }
        
        /**
         * Only matches exact colors, otherwise returns NONE.
         * @param exchangeHex
         * @return
         */
        public static ExchangeColor fromHex(String exchangeHex) {
            if (exchangeHex == null)
                return ExchangeColor.NONE;
            ExchangeColor exchangeColor = hexToExchangeColorMap.get(exchangeHex.trim().toLowerCase());
            if (exchangeColor == null)
                return ExchangeColor.NONE;
            return exchangeColor;
        }

        public int getColorIndex()
        {
            return colorIndex;
        }

        public String getColorName()
        {
            return colorName;
        }

        public String getHex()
        {
            return colorHex;
        }

        public ErgoColor getEquivalentErgoColor()
        {
            switch (this) {
//                case NONE:       return ErgoColor.NONE;
//                case RED:        return ErgoColor.CARDINAL;
//                case ORANGE:     return ErgoColor.ZEST;
//                case PEACH:      return ErgoColor.GOLDEN_DREAM;
//                case YELLOW:     return ErgoColor.GOLDEN_DREAM;
//                case GREEN:      return ErgoColor.SMALT_BLUE;
//                case TEAL:       return ErgoColor.SINBAD;
//                case OLIVE:      return ErgoColor.XANADU;
//                case BLUE:       return ErgoColor.SINBAD;
//                case PURPLE:     return ErgoColor.STRIKEMASTER;
//                case MAROON:     return ErgoColor.DUSTY_GRAY;
//                case STEEL:      return ErgoColor.IRON;
//                case DARKSTEEL:  return ErgoColor.NILE_BLUE;
//                case GRAY:       return ErgoColor.EDWARD;
//                case DARKGRAY:   return ErgoColor.XANADU;
//                case BLACK:      return ErgoColor.MIRAGE;
//                case DARKRED:    return ErgoColor.CARDINAL;
//                case DARKORANGE: return ErgoColor.CARDINAL;
//                case DARKPEACH:  return ErgoColor.BUTTERED_RUM;
//                case DARKYELLOW: return ErgoColor.BUTTERED_RUM;
//                case DARKGREEN:  return ErgoColor.SMALT_BLUE;
//                case DARKTEAL:   return ErgoColor.SMALT_BLUE;
//                case DARKOLIVE:  return ErgoColor.BUTTERED_RUM;
//                case DARKBLUE:   return ErgoColor.NILE_BLUE;
//                case DARKPURPLE: return ErgoColor.STRIKEMASTER;
//                case DARKMAROON: return ErgoColor.STRIKEMASTER;
//                default:         return ErgoColor.NONE;
                case NONE:       return ErgoColor.NONE;
                case RED:        return ErgoColor.RED;
                case ORANGE:     return ErgoColor.ORANGE;
                case PEACH:      return ErgoColor.PEACH;
                case YELLOW:     return ErgoColor.YELLOW;
                case GREEN:      return ErgoColor.GREEN;
                case TEAL:       return ErgoColor.TEAL;
                case OLIVE:      return ErgoColor.OLIVE;
                case BLUE:       return ErgoColor.BLUE;
                case PURPLE:     return ErgoColor.PURPLE;
                case MAROON:     return ErgoColor.MAROON;
                case STEEL:      return ErgoColor.STEEL;
                case DARKSTEEL:  return ErgoColor.DARKSTEEL;
                case GRAY:       return ErgoColor.GRAY;
                case DARKGRAY:   return ErgoColor.DARKGRAY;
                case BLACK:      return ErgoColor.BLACK;
                case DARKRED:    return ErgoColor.DARKRED;
                case DARKORANGE: return ErgoColor.DARKORANGE;
                case DARKPEACH:  return ErgoColor.DARKPEACH;
                case DARKYELLOW: return ErgoColor.DARKYELLOW;
                case DARKGREEN:  return ErgoColor.DARKGREEN;
                case DARKTEAL:   return ErgoColor.DARKTEAL;
                case DARKOLIVE:  return ErgoColor.DARKOLIVE;
                case DARKBLUE:   return ErgoColor.DARKBLUE;
                case DARKPURPLE: return ErgoColor.DARKPURPLE;
                case DARKMAROON: return ErgoColor.DARKMAROON;
                default:         return ErgoColor.NONE;
            }
        }

        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();

            buf.append("colorIndex: ").append(colorIndex);
            buf.append(", colorName: ").append(colorName);
            buf.append(", colorHex: ").append(colorHex);

            return buf.toString();
        }
    }

    public enum ErgoColor
    {
//        NONE         (-1, "None",         null),
//        SMALT_BLUE   (0,  "SmaltBlue",    "4d858d"),
//        NILE_BLUE    (1,  "NileBlue",     "184c5a"),
//        ZEST         (2,  "Zest",         "e18431"),
//        CARDINAL     (3,  "Cardinal",     "be1e2d"),
//        BUTTERED_RUM (4,  "ButteredRum",  "918c10"),
//        EDWARD       (5,  "Edward",       "9daeab"),
//        GOLDEN_DREAM (6,  "GoldenDream",  "edda22"),
//        STRIKEMASTER (7,  "Strikemaster", "815374"),
//        SINBAD       (8,  "Sinbad",       "a1cfca"),
//        SEA_PINK     (9,  "SeaPink",      "f0a693"),
//        LA_RIOJA     (10, "LaRioja",      "babb13"),
//        PINE_CONE    (11, "PineCone",     "706258"),
//        DUSTY_GRAY   (12, "DustyGray",    "b095a5"),
//        MIRAGE       (13, "Mirage",       "141d28"),
//        IRON         (14, "Iron",         "dadfe1"),
//        XANADU       (15, "Xanadu",       "728472");
        NONE       (-1, "None",       null),
        RED        (0,  "Red",        "e4636a"),
        ORANGE     (1,  "Orange",     "f49454"),
        PEACH      (2,  "Peach",      "ffca4c"),
        YELLOW     (3,  "Yellow",     "fffe3d"),
        GREEN      (4,  "Green",      "83d17a"),
        TEAL       (5,  "Teal",       "7bd2b5"),
        OLIVE      (6,  "Olive",      "b0c18a"),
        BLUE       (7,  "Blue",       "729bd9"),
        PURPLE     (8,  "Purple",     "9278d1"),
        MAROON     (9,  "Maroon",     "c382a2"),
        STEEL      (10, "Steel",      "c4ccdd"),
        DARKSTEEL  (11, "DarkSteel",  "4b5978"),
        GRAY       (12, "Gray",       "a8a8a8"),
        DARKGRAY   (13, "DarkGray",   "525252"),
        BLACK      (14, "Black",      "000000"),
        DARKRED    (15, "DarkRed",    "ab1d24"),
        DARKORANGE (16, "DarkOrange", "b14f0d"),
        DARKPEACH  (17, "DarkPeach",  "ab7b05"),
        DARKYELLOW (18, "DarkYellow", "999400"),
        DARKGREEN  (19, "DarkGreen",  "35792b"),
        DARKTEAL   (20, "DarkTeal",   "2e7d63"),
        DARKOLIVE  (21, "DarkOlive",  "5f6c3a"),
        DARKBLUE   (22, "DarkBlue",   "2a5191"),
        DARKPURPLE (23, "DarkPurple", "50328f"),
        DARKMAROON (24, "DarkMaroon", "82375f");
        
        private int colorIndex;
        private String colorName;
        private String colorHex;

        ErgoColor(int colorIndex, String colorName, String colorHex)
        {
            this.colorIndex = colorIndex;
            this.colorName = colorName;
            this.colorHex = colorHex;
            hexToErgoColorMap.put(colorHex, this);
        }

        public static ErgoColor fromColor(int colorIndex)
        {
            if (colorIndex < 0 || colorIndex >= ErgoColor.values().length)
                return ErgoColor.NONE;
            else
                return ErgoColor.values()[colorIndex+1];
        }
        
        /**
         * Only matches exact colors, otherwise returns NONE.
         * @param ergoHex
         * @return
         */
        public static ErgoColor fromHex(String ergoHex) {
            if (ergoHex == null)
                return ErgoColor.NONE;
            ErgoColor ergoColor = hexToErgoColorMap.get(ergoHex.trim().toLowerCase());
            if (ergoColor == null)
                return ErgoColor.NONE;
            return ergoColor;
        }

        public int getColorIndex()
        {
            return colorIndex;
        }

        public String getColorName()
        {
            return colorName;
        }

        public String getHex()
        {
            return colorHex;
        }
        
        public ExchangeColor getExchangeEquivalent()
        {
            switch (this) {
//                case NONE:         return ExchangeColor.NONE;
//                case SMALT_BLUE:   return ExchangeColor.DARKTEAL;
//                case NILE_BLUE:    return ExchangeColor.DARKSTEEL;
//                case ZEST:         return ExchangeColor.ORANGE;
//                case CARDINAL:     return ExchangeColor.DARKRED;
//                case BUTTERED_RUM: return ExchangeColor.DARKYELLOW;
//                case EDWARD:       return ExchangeColor.GRAY;
//                case GOLDEN_DREAM: return ExchangeColor.PEACH;
//                case STRIKEMASTER: return ExchangeColor.DARKMAROON;
//                case SINBAD:       return ExchangeColor.TEAL;
//                case SEA_PINK:     return ExchangeColor.MAROON;
//                case LA_RIOJA:     return ExchangeColor.DARKYELLOW;
//                case PINE_CONE:    return ExchangeColor.DARKGRAY;
//                case DUSTY_GRAY:   return ExchangeColor.MAROON;
//                case MIRAGE:       return ExchangeColor.BLACK;
//                case IRON:         return ExchangeColor.STEEL;
//                case XANADU:       return ExchangeColor.OLIVE;
            
                case NONE:          return ExchangeColor.NONE;
                case RED:           return ExchangeColor.RED;
                case ORANGE:        return ExchangeColor.ORANGE;
                case PEACH:         return ExchangeColor.PEACH;
                case YELLOW:        return ExchangeColor.YELLOW;
                case GREEN:         return ExchangeColor.GREEN;
                case TEAL:          return ExchangeColor.TEAL;
                case OLIVE:         return ExchangeColor.OLIVE;
                case BLUE:          return ExchangeColor.BLUE;
                case PURPLE:        return ExchangeColor.PURPLE;
                case MAROON:        return ExchangeColor.MAROON;
                case STEEL:         return ExchangeColor.STEEL;
                case DARKSTEEL:     return ExchangeColor.DARKSTEEL;
                case GRAY:          return ExchangeColor.GRAY;
                case DARKGRAY:      return ExchangeColor.DARKGRAY;
                case BLACK:         return ExchangeColor.BLACK;
                case DARKRED:       return ExchangeColor.DARKRED;
                case DARKORANGE:    return ExchangeColor.DARKORANGE;
                case DARKPEACH:     return ExchangeColor.DARKPEACH;
                case DARKYELLOW:    return ExchangeColor.DARKYELLOW;
                case DARKGREEN:     return ExchangeColor.DARKGREEN;
                case DARKTEAL:      return ExchangeColor.DARKTEAL;
                case DARKOLIVE:     return ExchangeColor.DARKOLIVE;
                case DARKBLUE:      return ExchangeColor.DARKBLUE;
                case DARKPURPLE:    return ExchangeColor.DARKPURPLE;
                case DARKMAROON:    return ExchangeColor.DARKMAROON;
                default:            return ExchangeColor.NONE;
            }
        }

        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();

            buf.append("colorName: ").append(colorName);
            buf.append(", colorHex: ").append(colorHex);

            return buf.toString();
        }
    }
}
