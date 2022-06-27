package tv.shout.sm.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.shout.snowyowl.domain.PayoutModel;
import tv.shout.snowyowl.domain.PayoutModelRound;
import tv.shout.util.JsonUtil;

//https://www.mkyong.com/java/apache-poi-reading-and-writing-excel-file-in-java/
public class PayoutModelInputter
{
    private String generateSqlInsertStatementsForPayoutModel(PayoutModel pm, int creatorId)
    {
        StringBuilder buf = new StringBuilder();

        buf.append(MessageFormat.format(
            "INSERT INTO snowyowl.payout_model (" +
            "`name`, base_player_count, entrance_fee_amount, creator_id, create_date) " +
            "VALUES (" +
            "''{0}'', {1,number,#}, {2,number,####.##}, {3,number,#}, NOW());\n",
            pm.getName(), pm.getBasePlayerCount(), pm.getEntranceFeeAmount(), creatorId));

        buf.append("SELECT LAST_INSERT_ID() INTO @PM_ID;\n");

        for (PayoutModelRound pmr : pm.getPayoutModelRounds()) {
            buf.append(MessageFormat.format(
                "\nINSERT INTO snowyowl.payout_model_round (" +
                "payout_model_id, sort_order, `description`, " +
                "starting_player_count, eliminated_player_count, eliminated_payout_amount, `type`, `category`" +
                ") VALUES (" +
                "@PM_ID, {0,number,#}, ''{1}'', " +
                "{2,number,#}, {3,number,#}, {4,number,####.##}, ''{5}'', ''{6}''" +
                ");",
                pmr.getSortOrder(), pmr.getDescription(), pmr.getStartingPlayerCount(), pmr.getEliminatedPlayerCount(), pmr.getEliminatedPayoutAmount(),
                pmr.getType(), pmr.getCategory()));
        }

        return buf.toString();
    }

    private PayoutModel getPayoutModelFromSpreadsheet(String filename, int sheetNumber)
    throws IOException
    {
        PayoutModel pm = new PayoutModel();
        try (FileInputStream excelFile = new FileInputStream(new File(filename))) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(sheetNumber);
            Iterator<Row> iterator = datatypeSheet.iterator();

            //read the PayoutModel information
            String payoutModelName = iterator.next().getCell(1).getStringCellValue();
            int basePlayerCount = (int) iterator.next().getCell(1).getNumericCellValue();
            float entranceFeeAmount = (float) iterator.next().getCell(1).getNumericCellValue();

            pm.setBasePlayerCount(basePlayerCount);
            pm.setEntranceFeeAmount(entranceFeeAmount);
            pm.setName(payoutModelName);

            //move to the round rows
            iterator.next();
            List<PayoutModelRound> pmrs = new ArrayList<>();

            int order = -1;
            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (row.getCell(0) == null) break;

                order++;

                String roundName = row.getCell(0).getStringCellValue();
                int startingPlayerCount = (int) row.getCell(1).getNumericCellValue();
                int eliminatedPlayerCount = (int) row.getCell(2).getNumericCellValue();
                float eliminatedPayoutAmount = (float) row.getCell(3).getNumericCellValue();
                String type = row.getCell(4).getStringCellValue();
                PayoutModelRound.CATEGORY category = PayoutModelRound.CATEGORY.valueOf(row.getCell(5).getStringCellValue());

                PayoutModelRound pmr = new PayoutModelRound();
                pmr.setDescription(roundName);
                pmr.setEliminatedPayoutAmount(eliminatedPayoutAmount);
                pmr.setEliminatedPlayerCount(eliminatedPlayerCount);
                pmr.setStartingPlayerCount(startingPlayerCount);
                pmr.setSortOrder(order);
                pmr.setType(type);
                pmr.setCategory(category);

                pmrs.add(pmr);
            }
            pm.setPayoutModelRounds(pmrs);
            workbook.close();
        }

        return pm;
    }

    private PayoutModel getPayoutModelFromJson(String filename)
    throws IOException
    {
        String json = new String(Files.readAllBytes(Paths.get(filename)));

        ObjectMapper jsonMapper = JsonUtil.getObjectMapper();
        TypeReference<PayoutModel> typeRef = new TypeReference<PayoutModel>() {};
        PayoutModel pm = jsonMapper.readValue(json, typeRef);

        return pm;
    }

    private String payoutModelToJson(PayoutModel pm)
    {
        ObjectMapper mapper = JsonUtil.getObjectMapper();
        try {
            return mapper.writeValueAsString(pm);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("unable to convert PayoutModel to json", e);
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        PayoutModelInputter pmi = new PayoutModelInputter();
        int creatorId = 8;

        //use spreadsheet
        //String filename = "/Users/shawker/Downloads/DM Payout Models.xlsx";
        //PayoutModel pm = pmi.getPayoutModelFromSpreadsheet(filename, 2);

        //use json
        String filename = "/Users/shawker/Downloads/data.json";
        PayoutModel pm = pmi.getPayoutModelFromJson(filename);

        String sqlInsertStatements = pmi.generateSqlInsertStatementsForPayoutModel(pm, creatorId);
        String payoutModelJson = pmi.payoutModelToJson(pm);

        System.out.println(pm);
        System.out.println("");
        System.out.println(sqlInsertStatements);
        System.out.println("");
        System.out.println(payoutModelJson);
    }

}
