package com.quant.repository;

import java.util.List;

import com.quant.entity.Signal;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SignalRepository {

    @Select("SELECT COUNT(*) FROM signal")
    long count();

    @Select("SELECT * FROM signal ORDER BY id")
    List<Signal> findAll();

    @Select("SELECT * FROM signal WHERE status = #{status}")
    List<Signal> findByStatus(@Param("status") String status);

    @Select("SELECT * FROM signal WHERE id = #{id}")
    Signal findById(@Param("id") long id);

    @Insert("""
            INSERT INTO signal (symbol, strategy, side, status, risk_message, price, quantity, created_at, version)
            VALUES (#{symbol}, #{strategy}, #{side}, #{status}, #{riskMessage}, #{price}, #{quantity},
                    #{createdAt}, #{version})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Signal signal);

    @Update("""
            UPDATE signal SET status = #{status}, version = COALESCE(version, 0) + 1
            WHERE id = #{id} AND status = 'PENDING' AND COALESCE(version, 0) = #{version}
            """)
    int updatePendingStatus(@Param("id") long id, @Param("status") String status,
            @Param("version") long version);
}
