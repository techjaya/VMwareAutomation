//@author Don Jayakody
package vCenterOps;

import java.sql.Timestamp;
import java.util.*;

public class PrintMsg {

	public static void PassPrint(String PassMsg) {

		System.out.println("\n-----------------------------------------------------------------------------------------");
		System.out.print("-vCenter LOG-: " + new Timestamp(System.currentTimeMillis()));
		System.out.println("	  PASS:" + PassMsg);
		System.out.println("-----------------------------------------------------------------------------------------\n");

	}

	public static void ErrorPrint(String ErrMsg) {

		System.out.println("\n-----------------------------------------------------------------------------------------");
		System.out.print(new Timestamp(System.currentTimeMillis()));
		System.out.println("	  ERROR: " + ErrMsg);
		System.out.println("-----------------------------------------------------------------------------------------\n");

	}

	public static void HeaderPrint(String Msg) {

		System.out.println();
		System.out.print("-vCenter LOG-: "+ new Timestamp(System.currentTimeMillis()));
		System.out.println("	" + Msg);
		System.out.println("===========================================================================================");

	}

	public static void ItemPrint(String Msg) {

		System.out.print("-vCenter LOG-: "+ new Timestamp(System.currentTimeMillis()));
		System.out.print("	" + Msg + "\n");

	}

	public static void MsgPrint(String Msg) {

		System.out.println();
		System.out.print("-vCenter LOG-: "+ new Timestamp(System.currentTimeMillis()));
		System.out.print("	" + Msg + "\n");

	}
	public static void TestCaseHeaderPrint(String Msg) {

		System.out.println("##############################################################################################################################################");
		System.out.print("-vCenter LOG-: "+ new Timestamp(System.currentTimeMillis()));
		System.out.println("	" + Msg);
		System.out.println("##############################################################################################################################################");

	}


}

