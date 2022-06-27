package tv.shout.ecommerce;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.meinc.store.domain.Item;
import com.meinc.store.domain.ItemPrice;

import tv.shout.sm.db.BaseDbSupport;
import tv.shout.sm.db.BaseDbSupport.SqlMapper;
import tv.shout.sm.db.DbProvider;

public class MockStoreService
{
    private static Logger _logger = Logger.getLogger(MockStoreService.class);
    private DbProvider _db;

    public MockStoreService(DbProvider.DB which)
    throws Exception
    {
        _db = new DbProvider(which);
    }

    public void stop()
    {
        _db.close();
    }

    public Item getItemByUuid(String itemUuid)
    {
        Item item;
        try {
            String sql = "SELECT * from `store`.`item` WHERE uuid = ?";

            SqlMapper<Item> sqlMapperForItem = new SqlMapper<Item>() {
                @Override
                public void populatePreparedStatement(PreparedStatement ps) throws SQLException
                {
                    ps.setString(1, itemUuid);
                }

                @Override
                public Item mapRowToType(ResultSet rs) throws SQLException
                {
                    Item item = new Item();
                    item.setItemId(rs.getInt("item_id"));
                    item.setStoreBundleId(rs.getString("store_bundle_id"));
                    item.setUuid(rs.getString("uuid"));
                    item.setTitle(rs.getString("title"));
                    item.setDescription(rs.getString("description"));
                    item.setPrice(rs.getString("price"));
                    item.setActive(rs.getBoolean("active"));

                    int quantity = rs.getInt("duration_quantity");
                    item.setDurationQuantity(rs.wasNull() ? null : quantity);

                    String unit = rs.getString("duration_unit");
                    item.setDurationUnit(rs.wasNull() ? null : Item.DurationUnit.valueOf(unit));

                    return item;
                }

                @Override
                public Collection<Item> getCollectionObject()
                {
                    return new ArrayList<>();
                }
            };

            List<Item> items = (List<Item>) BaseDbSupport.executeSqlForList(_db, sql, sqlMapperForItem);
            if (items == null || items.isEmpty()) {
                return null;
            }
            item = items.get(0);

            String priceSql = "SELECT * FROM store.item_price WHERE item_id = ?";

            SqlMapper<ItemPrice> sqlMapperForItemPrice = new SqlMapper<ItemPrice>() {
                @Override
                public void populatePreparedStatement(PreparedStatement ps) throws SQLException
                {
                    ps.setString(1, itemUuid);
                }

                @Override
                public ItemPrice mapRowToType(ResultSet rs) throws SQLException
                {
                    ItemPrice ip = new ItemPrice();

                    ip.setCurrencyCode(rs.getString("currency_code"));
                    ip.setPrice(rs.getDouble("price"));
                    ip.setFormattedPrice(rs.getString("formatted_price"));

                    return ip;
                }

                @Override
                public Collection<ItemPrice> getCollectionObject()
                {
                    return new ArrayList<>();
                }
            };

            List<ItemPrice> prices = (List<ItemPrice>) BaseDbSupport.executeSqlForList(_db, priceSql, sqlMapperForItemPrice);
            item.setItemPrice(prices);

        } catch (SQLException e) {
            _logger.error("sql exception", e);
            return null;
        }

        return item;
    }

    public void insertCustomerProfileMapping(int subscriberId, String customerProfileId)
    {
        try {
            String sql = "INSERT INTO store.customer_profile_mapping (subscriber_id, customer_profile_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE customer_profile_id = ?";

            Connection con = _db.getConnection();
            PreparedStatement ps = null;

            try {
                ps = con.prepareStatement(sql);
                ps.setInt(1, subscriberId);
                ps.setString(2, customerProfileId);
                ps.setString(3, customerProfileId);

                ps.execute();

            } finally {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            }

        } catch (SQLException e) {
            _logger.error("sql exception", e);
        }
    }

    public String getCustomerProfileMapping(int subscriberId)
    {
        try {
            String sql = "SELECT customer_profile_id FROM store.customer_profile_mapping WHERE subscriber_id = ?";

            SqlMapper<String> mapper = new SqlMapper<String>() {
                @Override
                public void populatePreparedStatement(PreparedStatement ps) throws SQLException
                {
                    ps.setInt(1, subscriberId);
                }

                @Override
                public String mapRowToType(ResultSet rs) throws SQLException
                {
                    return rs.getString(1);
                }

                @Override
                public Collection<String> getCollectionObject()
                {
                    return new ArrayList<>();
                }
            };

            List<String> items = (List<String>) BaseDbSupport.executeSqlForList(_db, sql, mapper);
            return items != null && items.size() > 0 ? items.get(0) : null;

        } catch (SQLException e) {
            _logger.error("sql exception", e);
            return null;
        }
    }

    public void deleteCustomerProfileMapping(int subscriberId)
    {
        try {
            String sql = "DELETE FROM store.customer_profile_mapping WHERE subscriber_id = ?";

            Connection con = _db.getConnection();
            PreparedStatement ps = null;

            try {
                ps = con.prepareStatement(sql);
                ps.setInt(1, subscriberId);
                ps.execute();

            } finally {
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (con != null) {
                    con.close();
                    con = null;
                }
            }

        } catch (SQLException e) {
            _logger.error("sql exception", e);
        }
    }
}
