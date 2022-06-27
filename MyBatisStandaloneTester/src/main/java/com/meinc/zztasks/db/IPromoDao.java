package com.meinc.zztasks.db;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.meinc.zztasks.domain.PromoBatch;
import com.meinc.zztasks.domain.PromoCode;

public interface IPromoDao
{
    @Select("SELECT COUNT(*) FROM `ergo`.`promo_auth_users` WHERE subscriber_id = #{0}")
    boolean isUserAuthorizedToAdministerPromoData(final int subscriberId);

    @Insert(
        "INSERT INTO `ergo`.`promo_batch` (" +
        "   `name`, create_date, creator_id, update_date, `status`, `type`, num_codes, unclaimed_expire_date) VALUES (" +
        "   #{name}, #{dateCreated}, #{creatorId}, #{lastUpdate}, #{status}, #{type}, #{numCodes}, #{unclaimedExpireDate})"
    )
    @Options(useGeneratedKeys=true, keyProperty="batchId")
    void insertPromoBatch(final PromoBatch batch);

    @Insert(
        "INSERT INTO `ergo`.`promo_code` (" +
        "   `code`, batch_id) VALUES (" +
        "   #{code.code}, #{1})"
    )
    void insertPromoCode(@Param("code") final PromoCode code, final int batchId);

    @Select("SELECT * FROM ergo.promo_batch")
    @Results({
        @Result(property="batchId", column="batch_id"),
        @Result(property="creatorId", column="creator_id"),
        @Result(property="dateCreated", column="create_date"),
        @Result(property="numCodes", column="num_codes"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="unclaimedExpireDate", column="unclaimed_expire_date"),
    })
    List<PromoBatch> getAllPromoBatches();

    @Select("SELECT * FROM ergo.promo_batch WHERE `status` =  #{0}")
    @Results({
        @Result(property="batchId", column="batch_id"),
        @Result(property="creatorId", column="creator_id"),
        @Result(property="dateCreated", column="create_date"),
        @Result(property="numCodes", column="num_codes"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="unclaimedExpireDate", column="unclaimed_expire_date"),
    })
    List<PromoBatch> getPromoBatchesByStatus(final PromoBatch.STATUS status);

    @Select("SELECT * FROM ergo.promo_batch WHERE batch_id = #{0}")
    @Results({
        @Result(property="batchId", column="batch_id"),
        @Result(property="creatorId", column="creator_id"),
        @Result(property="dateCreated", column="create_date"),
        @Result(property="numCodes", column="num_codes"),
        @Result(property="lastUpdate", column="update_date"),
        @Result(property="unclaimedExpireDate", column="unclaimed_expire_date"),
    })
    PromoBatch getPromoBatch(final int batchId);

    @Update(
        "UPDATE ergo.promo_batch " +
        "   SET `name` = #{name}, update_date = #{lastUpdate}, `status` = #{status}, `type` = #{type}, unclaimed_expire_date = #{unclaimedExpireDate} " +
        " WHERE batch_id = #{batchId}"
    )
    void updatePromoBatch(final PromoBatch batch);

    @Select("SELECT COUNT(*) FROM ergo.promo_code WHERE batch_id = #{0} AND `status` = 'ASSIGNED'")
    int getNumberAssignedPromoCodesForBatch(final int batchId);

    @Delete("DELETE FROM ergo.promo_code WHERE batch_id = #{0}")
    void deletePromoCodesForBatch(final int batchId);

    @Delete("DELETE FROM ergo.promo_batch WHERE batch_id = #{0}")
    void deletePromoBatch(final int batchId);

    @Select("SELECT * FROM ergo.promo_code WHERE batch_id = #{0}")
    @Results({
        @Result(property="codeId", column="code_id"),
        @Result(property="code", column="code"),
        @Result(property="batchId", column="batch_id"),
        @Result(property="status", column="status"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="dateUsed", column="used_date"),
        @Result(property="receiptId", column="receipt_id"),
    })
    List<PromoCode> getAllPromoCodesForBatch(int batchId);

    @Select("SELECT * FROM ergo.promo_code WHERE batch_id = #{0} AND `status` = #{1}")
    @Results({
        @Result(property="codeId", column="code_id"),
        @Result(property="code", column="code"),
        @Result(property="batchId", column="batch_id"),
        @Result(property="status", column="status"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="dateUsed", column="used_date"),
        @Result(property="receiptId", column="receipt_id"),
    })
    List<PromoCode> getPromoCodesForBatchByStatus(final int batchId, final PromoCode.STATUS status);

    @Select("SELECT * FROM ergo.promo_code WHERE code_id = #{0}")
    @Results({
        @Result(property="codeId", column="code_id"),
        @Result(property="code", column="code"),
        @Result(property="batchId", column="batch_id"),
        @Result(property="status", column="status"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="dateUsed", column="used_date"),
        @Result(property="receiptId", column="receipt_id"),
    })
    PromoCode getPromoCodeViaId(final int codeId);

    @Select("SELECT * FROM ergo.promo_code WHERE code = #{0}")
    @Results({
        @Result(property="codeId", column="code_id"),
        @Result(property="code", column="code"),
        @Result(property="batchId", column="batch_id"),
        @Result(property="status", column="status"),
        @Result(property="subscriberId", column="subscriber_id"),
        @Result(property="dateUsed", column="used_date"),
        @Result(property="receiptId", column="receipt_id"),
    })
    PromoCode getPromoCodeViaCode(final String code);

    @Update(
        "UPDATE ergo.promo_code " +
        "   SET `status` = 'ASSIGNED', subscriber_id = #{1}, used_date = NOW(), receipt_id = #{2} " +
        " WHERE code_id = #{0}"
    )
    void markPromoCodeAssigned(final int codeId, final int subscriberId, final int receiptId);
}
