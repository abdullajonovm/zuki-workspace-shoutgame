package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnsiCodes
{
    //http://ascii-table.com/ansi-escape-sequences.php
    public static final String ANSI_CLEAR_SCREEN = "\u001B[2J";
    public static final String CURSOR_UP = "\u001B[1A";
    public static final String CURSOR_DOWN = "\u001B[1B";
    public static final String CURSOR_FORWARD = "\u001B[1C";
    public static final String CURSOR_BACK = "\u001B[1D";

    //8 bit colors
    public static final String COLOR_8BIT_BLACK = "\u001B[30m";
    public static final String COLOR_8BIT_RED = "\u001B[31m";
    public static final String COLOR_8BIT_GREEN = "\u001B[32m";
    public static final String COLOR_8BIT_YELLOW = "\u001B[33m";
    public static final String COLOR_8BIT_BLUE = "\u001B[34m";
    public static final String COLOR_8BIT_MAGENTA = "\u001B[35m";
    public static final String COLOR_8BIT_CYAN = "\u001B[36m";
    public static final String COLOR_8BIT_WHITE = "\u001B[37m";

    //cursor
    public static final String ANSI_RESET_CURSOR = "\u001B[0m";
    public static final String ANSI_HIDE_CURSOR = "\u001B[?25l";
    public static final String ANSI_SHOW_CURSOR = "\u001B[?25h";
    public static final String ANSI_BLINK_CURSOR = "\u001B[5m";

    //box drawing shapes (more here: /https://en.wikipedia.org/wiki/Box-drawing_character)
    public static final String BOX_LINE_HORIZONTAL = "\u2500";
    public static final String BOX_LINE_VERTICAL = "\u2502";
    public static final String BOX_CORNER_SQUARE_TOP_LEFT = "\u250c";
    public static final String BOX_CORNER_SQUARE_TOP_RIGHT = "\u2510";
    public static final String BOX_CORNER_SQUARE_BOTTOM_LEFT = "\u2514";
    public static final String BOX_CORNER_SQUARE_BOTTOM_RIGHT = "\u2518";
    public static final String BOX_CORNER_ROUND_TOP_LEFT = "\u256d";
    public static final String BOX_CORNER_ROUND_TOP_RIGHT = "\u256e";
    public static final String BOX_CORNER_ROUND_BOTTOM_LEFT = "\u2570";
    public static final String BOX_CORNER_ROUND_BOTTOM_RIGHT = "\u256f";
    public static final String BOX_SPLIT_LEFT = "\u2524";
    public static final String BOX_SPLIT_RIGHT = "\u251c";


    //many things, including 16bit,32bit colors: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html

    public void moveToPos(int row, int col)
    {
        char escCode = 0x1B;
        System.out.print(String.format("%c[%d;%df",escCode,row,col));
    }

    public void drawBox(int row, int col, int width, int height, String boxColor)
    {
        System.out.print(boxColor);

        //top line
        moveToPos(row, col);
        System.out.print(BOX_CORNER_SQUARE_TOP_LEFT);
        for (int i=0; i<width-2; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_CORNER_SQUARE_TOP_RIGHT);

        //left side
        moveToPos(row, col);
        for (int i=0; i<height-2; i++) System.out.print(CURSOR_DOWN + BOX_LINE_VERTICAL + CURSOR_BACK);

        //bottom line
        System.out.print(CURSOR_DOWN + BOX_CORNER_SQUARE_BOTTOM_LEFT);
        for (int i=0; i<width-2; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_CORNER_SQUARE_BOTTOM_RIGHT);

        //right side
        for (int i=0; i<height-2; i++) System.out.print(CURSOR_UP + CURSOR_BACK + BOX_LINE_VERTICAL);
    }

    /**
     * @param row .
     * @param col .
     * @param widt the inner width (not counting the border)
     * @param height the inner height (not counting the border)
     * @param title .
     * @param boxColor .
     * @param titleColor .
     */
    public void drawBoxWithTitle(int row, int col, int width, int height, String title, String boxColor, String titleColor)
    {
        int minWidth = title.length() + 6;
        width = width < minWidth ? minWidth : width;
        int minHeight = 5;
        height = height < minHeight ? minHeight : height;

        int titleBoxStartCol = col + width / 2 - title.length() / 2 - 2;


        //title top row
        System.out.print(boxColor);
        moveToPos(row, titleBoxStartCol+1);
        System.out.print(BOX_CORNER_ROUND_TOP_LEFT);
        for (int i=0; i<title.length()+2; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_CORNER_ROUND_TOP_RIGHT);

        //top row (w/title)
        moveToPos(row+1, col);
        System.out.print(BOX_CORNER_SQUARE_TOP_LEFT);
        for (int i=col; i<titleBoxStartCol; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_SPLIT_LEFT + " " + titleColor);
        System.out.print(title + " " + boxColor + BOX_SPLIT_RIGHT);
        for (int i=titleBoxStartCol + 2 + title.length() + 2; i<col+width-1; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_CORNER_SQUARE_TOP_RIGHT);

        //title bottom row
        moveToPos(row+2, col);
        System.out.print(BOX_LINE_VERTICAL);
        moveToPos(row+2, titleBoxStartCol+1);
        System.out.print(BOX_CORNER_ROUND_BOTTOM_LEFT);
        for (int i=0; i<title.length()+2; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_CORNER_ROUND_BOTTOM_RIGHT);
        moveToPos(row+2, col+width);
        System.out.print(BOX_LINE_VERTICAL);

        //left side
        moveToPos(row+2, col);
        for (int i=0; i<height-1; i++) System.out.print(CURSOR_DOWN + BOX_LINE_VERTICAL + CURSOR_BACK);

        //right side
        moveToPos(row+2, col+width);
        for (int i=0; i<height-1; i++) System.out.print(CURSOR_DOWN + BOX_LINE_VERTICAL + CURSOR_BACK);

        //bottom
        moveToPos(row+height+1, col);
        System.out.print(BOX_CORNER_SQUARE_BOTTOM_LEFT);
        for (int i=0; i<width-1; i++) System.out.print(BOX_LINE_HORIZONTAL);
        System.out.print(BOX_CORNER_SQUARE_BOTTOM_RIGHT);

    }

    public void displayText(int row, int col, String text, String textColor)
    {
        moveToPos(row, col);
        System.out.print(textColor + text);
    }

    public void displayNumberedList(int row, int col, List<String> items, String textColor)
    {
        System.out.print(textColor);
        for (int i=0; i<items.size(); i++) {
            String item = items.get(i);
            moveToPos(row+i, col);
            System.out.print((i+1) + ". " + item);
        }
    }

    public void displayList(int row, int col, List<String> items, String textColor)
    {
        System.out.print(textColor);
        for (int i=0; i<items.size(); i++) {
            String item = items.get(i);
            moveToPos(row+i, col);
            System.out.print(item);
        }
    }

    /**
     * @param unicode a hex val, such as: 0x1f4a3
     * @return a string representation of the unicode character
     */
    public static String unicodeToString(int unicode)
    {
        return new String(Character.toChars(unicode));
    }

    public static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        AnsiCodes AnsiUiToolkit = new AnsiCodes();
        System.out.print(ANSI_CLEAR_SCREEN);
        AtomicBoolean running = new AtomicBoolean(true);

        try {
//            s.drawBox(1, 1, 103, 27, ANSI_8BIT_RED);
//            s.drawBoxWithTitle(3, 3, 40, 10, "TITLE HERE", ANSI_8BIT_CYAN, ANSI_8BIT_GREEN);
//            s.drawBoxWithTitle(2, 50, 50, 12, "Dogs v. Aliens", ANSI_8BIT_MAGENTA, ANSI_8BIT_YELLOW);
//            s.drawBoxWithTitle(15, 6, 37, 8, "Boring Box", ANSI_8BIT_BLACK, ANSI_8BIT_WHITE);
//
//            List<String> items = Arrays.asList("Refresh Data", "Show by enemyType", "Show by attackType", "Exit");
//            s.displayNumberedList(6, 5, items, ANSI_8BIT_WHITE);
////            s.displayText(6, 5, "1. Refresh Data", ANSI_8BIT_WHITE);
////            s.displayText(7, 5, "2. Show by enemyType", ANSI_8BIT_WHITE);
////            s.displayText(8, 5, "3. Show by attackType", ANSI_8BIT_WHITE);
////            s.displayText(9, 5, "4. Exit", ANSI_8BIT_WHITE);
//
//            System.out.print(ANSI_BLINK_CURSOR);
//            String bombEmoji = AnsiCodes.unicodeToString(0x1f4a3);
//            s.displayText(20, 17, bombEmoji + " RED ALERT 💣", ANSI_8BIT_RED);
//            System.out.print(ANSI_RESET_CURSOR);
//
//            //display the date in a box
//            int boxWidth = new Date().toString().length()+5;
//            int top = 18;
//            int left = 55;
//            s.drawBox(top, left, boxWidth, 3, ANSI_8BIT_CYAN);
//
//            System.out.print(ANSI_HIDE_CURSOR);
//
//            new Thread() {
//                @Override
//                public void run() {
//                    while (running.get()) {
//                        s.displayText(top+1, left+2, new Date().toString(), ANSI_8BIT_GREEN);
//                        try {
//                            Thread.sleep(1_000L);
//                        } catch (InterruptedException e) {
//                            running.set(false);
//                        }
//                    }
//                }
//            }.start();
//
//            s.moveToPos(30, 1);
//
//            //wait for user input
//            String consoleInput = "";
//            while (running.get()) {
//                consoleInput = AnsiCodes.getConsoleInput("");
//                switch (consoleInput)
//                {
//                    case "4": running.set(false); break;
//                }
//            }

            //Main Menu
            AnsiUiToolkit.drawBox(1, 1, 65, 24, AnsiUiToolkit.COLOR_8BIT_GREEN);
            AnsiUiToolkit.drawBoxWithTitle(2, 2, 30, 8, "Menu", AnsiUiToolkit.COLOR_8BIT_CYAN, AnsiUiToolkit.COLOR_8BIT_MAGENTA);
            AnsiUiToolkit.displayNumberedList(5, 4, Arrays.asList("Pick Enemy Type", "Pick Attack Type", "Select Player", "Refresh", "Exit"), AnsiUiToolkit.COLOR_8BIT_WHITE);

            AnsiUiToolkit.drawBoxWithTitle(2, 34, 30, 8, "Summary", AnsiUiToolkit.COLOR_8BIT_YELLOW, AnsiUiToolkit.COLOR_8BIT_RED);
            AnsiUiToolkit.displayList(5, 36, Arrays.asList(
                "# attacks: 581", "crit %: 30", "total dmg: 4,356", "avg dmg: 25", "avg crit dmg: 99", "avg !crit dmg: 66"
            ), AnsiUiToolkit.COLOR_8BIT_CYAN);

            AnsiUiToolkit.drawBoxWithTitle(13, 2, 30, 9, "Enemy Stats", AnsiUiToolkit.COLOR_8BIT_YELLOW, AnsiUiToolkit.COLOR_8BIT_RED);
            AnsiUiToolkit.displayText(16, 3, "Hardened Skeleton Archer", AnsiUiToolkit.COLOR_8BIT_RED);
            AnsiUiToolkit.displayList(17, 4, Arrays.asList(
                "# attacks: 581", "crit %: 30", "total dmg: 4,356", "avg dmg: 25", "avg crit dmg: 99", "avg !crit dmg: 66"
            ), AnsiUiToolkit.COLOR_8BIT_CYAN);

            AnsiUiToolkit.drawBoxWithTitle(13, 34, 30, 9, "Attack Stats", AnsiUiToolkit.COLOR_8BIT_YELLOW, AnsiUiToolkit.COLOR_8BIT_RED);
            AnsiUiToolkit.displayText(16, 35, "Whirling Blades", AnsiUiToolkit.COLOR_8BIT_RED);
            AnsiUiToolkit.displayList(17, 36, Arrays.asList(
                    "# attacks: 581", "crit %: 30", "total dmg: 4,356", "avg dmg: 25", "avg crit dmg: 99", "avg !crit dmg: 66"
                ), AnsiUiToolkit.COLOR_8BIT_CYAN);

//            //Waiting for data
//            AnsiUiToolkit.drawBox(1, 1, 65, 24, AnsiUiToolkit.COLOR_8BIT_GREEN);
//            AnsiUiToolkit.displayText(12, 25, "Loading Data ...", AnsiUiToolkit.COLOR_8BIT_YELLOW);

//            //Select character
//            AnsiUiToolkit.drawBox(1, 1, 65, 24, AnsiUiToolkit.COLOR_8BIT_GREEN);
//            AnsiUiToolkit.drawBoxWithTitle(2, 2, 30, 20, "Select Character", AnsiUiToolkit.COLOR_8BIT_CYAN, AnsiUiToolkit.COLOR_8BIT_MAGENTA);
//            AnsiUiToolkit.displayNumberedList(5, 4, Arrays.asList("Feldon Grimshaw", "Callistrotta Vici", "Phoenix Arizona", "Volana Falcon"), AnsiUiToolkit.COLOR_8BIT_WHITE);

//            //Select enemy type
//            AnsiUiToolkit.drawBox(1, 1, 65, 24, AnsiUiToolkit.COLOR_8BIT_GREEN);
//            AnsiUiToolkit.drawBoxWithTitle(2, 2, 30, 20, "Select Enemy Type", AnsiUiToolkit.COLOR_8BIT_CYAN, AnsiUiToolkit.COLOR_8BIT_MAGENTA);
//            AnsiUiToolkit.displayNumberedList(5, 4, Arrays.asList("Ferocious Red Spider", "Hardened Skeleton Archer", "Lich Mage", "Troll Twin"), AnsiUiToolkit.COLOR_8BIT_WHITE);

//            //Select attach type
//            AnsiUiToolkit.drawBox(1, 1, 65, 24, AnsiUiToolkit.COLOR_8BIT_GREEN);
//            AnsiUiToolkit.drawBoxWithTitle(2, 2, 30, 20, "Select Attack Type", AnsiUiToolkit.COLOR_8BIT_CYAN, AnsiUiToolkit.COLOR_8BIT_MAGENTA);
//            AnsiUiToolkit.displayNumberedList(5, 4, Arrays.asList("Rend", "basic", "Double Slash", "Galvanic Blast"), AnsiUiToolkit.COLOR_8BIT_WHITE);

            AnsiUiToolkit.moveToPos(30, 1);

        } finally {
            System.out.println(ANSI_SHOW_CURSOR + ANSI_RESET_CURSOR);
        }
    }

}
