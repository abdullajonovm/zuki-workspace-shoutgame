package com.shout.unixwareutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.meinc.gameplay.domain.Tuple;

public class SoFileListComparator
{
    private static final String CONFIG_DIR = "src/main/resources";

//    public List<String> getSoFilesInDir(String dir)
//    throws IOException
//    {
//        return Files.list(Paths.get(dir))
//            .filter(p -> p.toFile().isFile())
//            .map(p -> p.toFile().getAbsolutePath())
//            .filter(fn -> fn.toLowerCase().endsWith(".so"))
//            .map(fn -> {
//                String[] parts = fn.split("/");
//                return parts[parts.length-1];
//            })
//            .collect(Collectors.toList());
//  }

    public List<String> getSoFilesFromFileListing(String filenameInConfigDir)
    throws IOException
    {
        return Files.lines(Paths.get(CONFIG_DIR + "/" + filenameInConfigDir))
//            .map(fn -> {
//                String[] parts = fn.split("/");
//                return parts[parts.length-1];
//            })
            .collect(Collectors.toList());
    }

    public List<Tuple<String>> getUniionOfSoFileLists(List<String> l1, List<String> l2)
    {
        //get only the filename (without the path) for the first list
        List<String> l1NameOnly = l1.stream()
            .map(fn -> {
                String[] parts = fn.split("/");
                return parts[parts.length-1];
            })
            .collect(Collectors.toList());

        //get only the filename (without the path) for the second list
        List<String> l2NameOnly = l2.stream()
                .map(fn -> {
                    String[] parts = fn.split("/");
                    return parts[parts.length-1];
                })
                .collect(Collectors.toList());

        List<Tuple<String>> result = new ArrayList<>();

        //for every file in the first list, compare against every file in the second list
        for (int i=0; i<l1.size(); i++) {
            String l1Name = l1NameOnly.get(i);

            for (int j=0; j<l2.size(); j++) {
                String l2Name = l2NameOnly.get(j);

                if (l1Name.equalsIgnoreCase(l2Name)) {
                    //a filename match; add the tuple (using the full path, not just the filename)
                    Tuple<String> tuple = new Tuple<>();
                    tuple.setKey(l1.get(i));
                    tuple.setVal(l2.get(j));
                    result.add(tuple);
                }
            }
        }

        return result;
    }

    public static void main(String[] args)
    throws IOException
    {
        SoFileListComparator obj = new SoFileListComparator();

        List<String> unixware210SoFiles = obj.getSoFilesFromFileListing("uw210_so_files.txt");
        List<String> unixware213SoFiles = obj.getSoFilesFromFileListing("uw213_so_files.txt");
        List<String> unixware711SoFiles = obj.getSoFilesFromFileListing("uw711_so_files.txt");
        List<String> unixware712SoFiles = obj.getSoFilesFromFileListing("uw712_so_files.txt");
        List<String> unixwareGeminiSoFiles = obj.getSoFilesFromFileListing("uwGemini_so_files.txt");

        List<String> redhat68SoFiles = obj.getSoFilesFromFileListing("rhel6.8_so_files.txt");

        List<String> l1 = Arrays.asList("./foo/bar/x", "y", "z");
        List<String> l2 = Arrays.asList("a", "b", "c", "z", "p", "d", "q", "./a/b/c/x");
        List<Tuple<String>> test = obj.getUniionOfSoFileLists(l1, l2);

        List<Tuple<String>> uw210Rh68SoUnion = obj.getUniionOfSoFileLists(unixware210SoFiles, redhat68SoFiles);
        List<Tuple<String>> uw213Rh68SoUnion = obj.getUniionOfSoFileLists(unixware213SoFiles, redhat68SoFiles);
        List<Tuple<String>> uw711Rh68SoUnion = obj.getUniionOfSoFileLists(unixware711SoFiles, redhat68SoFiles);
        List<Tuple<String>> uw712Rh68SoUnion = obj.getUniionOfSoFileLists(unixware712SoFiles, redhat68SoFiles);
        List<Tuple<String>> uwGeminiRh68SoUnion = obj.getUniionOfSoFileLists(unixwareGeminiSoFiles, redhat68SoFiles);

//        uw210Rh68SoUnion.forEach(tp -> { System.out.println(tp.getKey() + " === " + tp.getVal()); });
//        uw213Rh68SoUnion.forEach(tp -> { System.out.println(tp.getKey() + " === " + tp.getVal()); });
//        uw711Rh68SoUnion.forEach(tp -> { System.out.println(tp.getKey() + " === " + tp.getVal()); });
//        uw712Rh68SoUnion.forEach(tp -> { System.out.println(tp.getKey() + " === " + tp.getVal()); });
//        uwGeminiRh68SoUnion.forEach(tp -> { System.out.println(tp.getKey() + " === " + tp.getVal()); });

        test.forEach(tp -> { System.out.println(tp.getKey() + " === " + tp.getVal()); });
    }

}
