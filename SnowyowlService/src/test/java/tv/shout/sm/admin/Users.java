package tv.shout.sm.admin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tv.shout.sm.db.DbProvider;

public interface Users
{
//    static User dc4_shawker = new User(
//        8, "shawker@me-inc.com", "c}LUK#}.99gUK!<q:r1f-I1V7MysCbAT", "9ef4f9ee39676c401bea708661283921bb2c72bc2f95a30bb38b5a9fd485a997", "5e47ce76-7faa-426f-a8cf-27c106cb86eb", "6170b6711e251ad6"
//    );
    static User local_shawker = new User(
            8, "shawker@me-inc.com", "c}LUK#}.99gUK!<q:r1f-I1V7MysCbAT", "9ef4f9ee39676c401bea708661283921bb2c72bc2f95a30bb38b5a9fd485a997", "5e47ce76-7faa-426f-a8cf-27c106cb86eb", "6170b6711e251ad6"
        );
    static User nc10_1_shawker = new User(
            8, "shawker@me-inc.com", "c}LUK#}.99gUK!<q:r1f-I1V7MysCbAT", "9ef4f9ee39676c401bea708661283921bb2c72bc2f95a30bb38b5a9fd485a997", "008e37e4-fd89-4512-a690-76bbf7fb7bfd", "6170b6711e251ad6"
        );
    static User nc11_1_shawker = new User(
            8, "shawker@me-inc.com", "c}LUK#}.99gUK!<q:r1f-I1V7MysCbAT", "9ef4f9ee39676c401bea708661283921bb2c72bc2f95a30bb38b5a9fd485a997", "5e47ce76-7faa-426f-a8cf-27c106cb86eb", "6170b6711e251ad6"
        );

//    static List<User> dc4_users = Arrays.asList(dc4_shawker);
    static List<User> local_users = Arrays.asList(local_shawker);
    static List<User> nc10_1_users = Arrays.asList(nc10_1_shawker);
    static List<User> nc11_1_users = Arrays.asList(nc11_1_shawker);

//    static Map<String, User> dc4_userMap = new HashMap<>();
    static Map<String, User> local_userMap = new HashMap<>();
    static Map<String, User> nc10_1_userMap = new HashMap<>();
    static Map<String, User> nc11_1_userMap = new HashMap<>();

    static String getUserEmails(DbProvider.DB which)
    {
        String result;
        switch (which)
        {
//            case DC4: {
//                result = dc4_users.stream().map(u -> u.email).collect(Collectors.joining(","));
//            }
//            break;

            case LOCAL: {
                result = local_users.stream().map(u -> u.email).collect(Collectors.joining(","));
            }
            break;

            case NC10_1: {
                result = nc10_1_users.stream().map(u -> u.email).collect(Collectors.joining(","));
            }
            break;

            case NC11_1: {
                result = nc11_1_users.stream().map(u -> u.email).collect(Collectors.joining(","));
            }
            break;

            default:
                throw new IllegalArgumentException("unsupported db: " + which);
        }

        return result;
    }

    static User getUser(DbProvider.DB which, String email)
    {
        //lazy initialize the maps
//        if (dc4_userMap.size() == 0) {
//            dc4_users.stream().forEach(u -> {
//                dc4_userMap.put(u.email, u);
//            });
//        }
        if (local_userMap.size() == 0) {
            local_users.stream().forEach(u -> {
                local_userMap.put(u.email, u);
            });
        }
        if (nc10_1_userMap.size() == 0) {
            nc10_1_users.stream().forEach(u -> {
                nc10_1_userMap.put(u.email, u);
            });
        }
        if (nc11_1_userMap.size() == 0) {
            nc11_1_users.stream().forEach(u -> {
                nc11_1_userMap.put(u.email, u);
            });
        }

        Map<String, User> map;
        switch (which)
        {
//            case DC4: {
//                map = dc4_userMap;
//            }
//            break;

            case LOCAL: {
                map = local_userMap;
            }
            break;

            case NC10_1: {
                map = nc10_1_userMap;
            }
            break;

            case NC11_1: {
                map = nc11_1_userMap;
            }
            break;

            default:
                throw new IllegalArgumentException("unsupported db: " + which);
        }

        return map.get(email);
    }
}
