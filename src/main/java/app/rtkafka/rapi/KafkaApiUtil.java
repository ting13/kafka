package app.rtkafka.rapi;

import java.sql.SQLException;
import java.util.Date;

import above.Above;
import above.db.AbDbRows;
import dbmgr.EDb;
import lst.Lst;
import rdb.CacheTime;
import rdb.DbObjsRows0;
import rdb.Preps;
import rdb.intfs.ISql;
import row.ObjsRow;
import times.Day;
import twdb.entity.TdccEquity;

/**
 * 用來作為 API 拉資料的 Utility Class
 * 
 * @author ericlin
 *
 */
public class KafkaApiUtil {


	/**
	 * 測試 DB Connection 的 Method
	 * @return
	 */
	public static Lst<TdccEquity> fetchTdccEquity() {
		
		String startDate = "2018-08-01";
		String endDate = "2018-09-20";
		int level = 15;
		
		Lst<TdccEquity> lstRet = Lst.of();
		
		String sSql = "SELECT * FROM TdccEquity WHERE Date >= ? AND Date <= ? AND NumSeq >= ? AND NumSeq <= 15 AND LENGTH(Symbol)=4";

		AbDbRows rows;
		try {
			rows = Above.dbmgr().select(EDb.tw, ISql.of(sSql), Preps.of(startDate, endDate, level), CacheTime.ofHour(12));
			lstRet = rows.toLst_bySf(TdccEquity.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return lstRet;
	}

	/**
	 * 拿出目前 honset 的最後 CalcDate
	 * @return
	 */
	public static String fetchCalcDate() {
		String sDate = "";
		String sSql = "SELECT Max(Date) from CalcDate";
		try {
			DbObjsRows0 lstDates = Above.dbmgr().select(EDb.app, ISql.of(sSql));
			if (lstDates != null && lstDates.size() == 1) {
				ObjsRow row = lstDates.getRow(0);
				Date date = row.getDate(0);
				sDate = Day.of(date).toString();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return sDate;
	}
	
}
