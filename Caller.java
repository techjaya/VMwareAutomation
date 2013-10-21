package vCenterOps;

import java.net.URL;
import java.io.*;
import java.util.*;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

public class Caller {
	

	private static ArrayList<String> switchList = new ArrayList<String>();
	private static ArrayList<String> vCenteripList = new ArrayList<String>();
	private static ArrayList<String> unameList = new ArrayList<String>();
	private static ArrayList<String> passwordList = new ArrayList<String>();
	private static ArrayList<String> vCenternameList = new ArrayList<String>();
	private static ArrayList<String> datacenterList = new ArrayList<String>();
	private static ArrayList<String> hostList = new ArrayList<String>();
	private static ArrayList<String> deletehostList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> hostvmnicMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> hostInterfaceMap = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> datastoreList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> vmMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> clonevmMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> renamevmMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> deletevmMap = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> stdvnicList = new ArrayList<String>();
	private static ArrayList<String> dvnicList = new ArrayList<String>();
	private static ArrayList<String> vswitchList = new ArrayList<String>();
	private static ArrayList<String> deletevssList = new ArrayList<String>();
	private static ArrayList<String> dvswitchList = new ArrayList<String>();
	private static ArrayList<String> deletedvsList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> pguplinkMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> pgMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<Integer>> pgvlanMap = new HashMap<String, ArrayList<Integer>>();
	private static Map<String, ArrayList<String>> dvpguplinkMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> dvpgMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> dvsldpMap = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<Integer>> dvpgvlanMap = new HashMap<String, ArrayList<Integer>>();
	private static ArrayList<String> addpgList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> addpgMap = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> adddvpgList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> adddvpgMap = new HashMap<String, ArrayList<String>>();
	private static ArrayList<Integer> addpgvlanList = new ArrayList<Integer>();
	private static ArrayList<String> adddvpgvlanList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> addstdvmkMap = new HashMap<String, ArrayList<String>>();
	private static ArrayList<String> changestdvnicList = new ArrayList<String>();
	private static Map<String, ArrayList<String>> changestdvnicMap = new HashMap<String, ArrayList<String>>();

	private static int globalNumvSwitches = 0;
	private static int globalNumDvSwitches = 0;
	private static int globalNumVms = 0;
	private static int globalNumStdVnics = 0;
	private static int globalNumDvnics = 0;
	private static boolean  globalAddPGCountFlag = false; 
	private static boolean  globalAddDvPGCountFlag = false; 
	private static boolean delayFlag = false;

	public static void main (String[] args) throws Exception {
		
		if (args.length == 2) {
			if (args[1].equals("Delay")) {
				delayFlag = true;
			} else {
				System.out.println("Usage: java -cp .:dom4j-1.6.1.jar:vijava5120121125.jar vCenterOps.Caller <COMMAND> <Delay>");
				System.exit(0);
			}
		}
		else if (args.length != 1) {
			System.out.println("Usage: java -cp .:dom4j-1.6.1.jar:vijava5120121125.jar vCenterOps.Caller <COMMAND> <Optional: Delay>");
			System.exit(0);
		}
		
    		String[] testcaseList = {"AddvSwitch", "AddDvSwitch", "CreateVM", "CloneVM", "RenameVM", "AddPGs", "AddDvPGs", "AddPGVlans", "AddDvPGVlans", "AddStdVnic", "AddDvnic", "AddStdVMks", "DeleteStdVMks", "DeleteStdVnic", "DeleteDvnic",  "DeletePGs", "DeleteDvPGs", "DeleteVM", "DeletevSwitch", "DeleteDvSwitch", "ChangeStdVnics", "Rebuild"} ;

		boolean gotCommand = false;
		for(int i=0; i<testcaseList.length; i++) {
			if (testcaseList[i].equals(args[0])) {
				gotCommand = true;
				break;
			}
		}
		if(!gotCommand) {
			System.out.println("Please Enter A Valid Command: ");
			for (int i=0; i<testcaseList.length; i++) {
				System.out.println("\t " + testcaseList[i]);
			}
			System.out.println();
			System.exit(0);
		}
	

		ValidateInventory("autoInventory.txt");
		ReadInventory("autoInventory.txt");	


		try {

			String vCenterIP =  "https://".concat(vCenteripList.get(0)).concat("/sdk");
			String uname = unameList.get(0);
			String password = passwordList.get(0);
 
			ServiceInstance si = new ServiceInstance(new URL(vCenterIP), uname, password, true);

			//Write the currentDC name to the CurrentDCName.txt
			String dcFileName = "./RunOutput/CurrentDCName.txt";
			BufferedWriter bwDC = writeFile(dcFileName);
			bwDC.write(datacenterList.get(0));
			bwDC.write("\n");
			bwDC.close();

			try {
				String opCall = args[0];

				PrintMsg.TestCaseHeaderPrint("TEST CASE:  " + opCall);

//				DiscoverVcenter(si, "AutoDC");

				if (opCall.equals("CreateVM")) {
					String status = VMOps.CreateVM(si, datacenterList, datastoreList, vmMap, stdvnicList, dvnicList, hostList, dvswitchList, dvpgMap, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.PassPrint("CreateVM : Success");
					} else {
						throw new Exception ("Error during CreateVM");
					}

				} else if (opCall.equals("CloneVM")) {
					String status = VMOps.CloneVM(si, vmMap, clonevmMap, hostList, dvswitchList, dvpgMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.PassPrint("CloneVM : Success");
					} else {
						throw new Exception ("Error during CloneVM");
					}

				} else if (opCall.equals("RenameVM")) {
					String status = VMOps.RenameVM(si, vmMap, renamevmMap, hostList, dvswitchList, dvpgMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.PassPrint("RenameVM : Success");
					} else {
						throw new Exception ("Error during RenameVM");
					}

				} else if (opCall.equals("DeleteVM")) {
					String status = VMOps.DeleteVM(si, deletevmMap, hostList, dvswitchList, dvpgMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.PassPrint("DeleteVM : Success");
					} else {
						throw new Exception ("Error during DeleteVM");
					}

				} else if (opCall.equals("AddvSwitch")) {
					String status = NetworkOps.AddvSwitch(si, vswitchList, hostList, pgMap, pgvlanMap, pguplinkMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddvSwitch : Success");
					} else {
						throw new Exception ("Error during AddvSwitch");
					}

				} else if (opCall.equals("DeletevSwitch")) {
					String status = NetworkOps.DeletevSwitch(si, deletevssList, hostList, datacenterList, pgMap, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeletevSwitch : Success");
					} else {
						throw new Exception ("Error during DeletevSwitch");
					}

				} else if (opCall.equals("AddDvSwitch")) {
					String status = NetworkOps.AddDvSwitch(si, dvswitchList, hostList, dvpgMap, dvpgvlanMap, dvpguplinkMap, dvsldpMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddDvSwitch : Success");
					} else {
						throw new Exception ("Error during AddDvSwitch");
					}

				} else if (opCall.equals("DeleteDvSwitch")) {
					String status = NetworkOps.DeleteDvSwitch(si, deletedvsList, dvpgMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeleteDvSwitch : Success");
					} else {
						throw new Exception ("Error during DeleteDvSwitch");
					}

				} else if (opCall.equals("DeleteHost")) {
					String status = VMOps.DeleteHost(si, deletehostList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeleteHost : Success");
					} else {
						throw new Exception ("Error during DeleteHost");
					}

				} else if (opCall.equals("AddHost")) {
					String status = VMOps.AddHost(si, datacenterList, deletehostList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddHost : Success");
					} else {
						throw new Exception ("Error during AddHost");
					}

				} else if (opCall.equals("AddPGs")) {
					String status = NetworkOps.AddPGs(si,hostList, addpgList, addpgMap, addpgvlanList, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddPGs : Success");
					} else {
						throw new Exception ("Error during AddPGs");
					}

				} else if (opCall.equals("DeletePGs")) {
					String status = NetworkOps.DeletePGs(si,hostList, addpgList, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeletePGs : Success");
					} else {
						throw new Exception ("Error during DeletePGs");
					}

				} else if (opCall.equals("AddDvPGs")) {
					String status = NetworkOps.AddDvPGs(si,adddvpgList, adddvpgMap,  datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddDvPGs : Success");
					} else {
						throw new Exception ("Error during AddDvPGs");
					}

				} else if (opCall.equals("DeleteDvPGs")) {
					String status = NetworkOps.DeleteDvPGs(si,adddvpgList, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeleteDvPGs : Success");
					} else {
						throw new Exception ("Error during DeleteDvPGs");
					}

				} else if (opCall.equals("AddPGVlans")) {
					String status = NetworkOps.AddPGVlans(si, hostList, addpgvlanList, addpgList, addpgMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddPGVlans : Success");
					} else {
						throw new Exception ("Error during AddPGVlans");
					}


				} else if (opCall.equals("AddDvPGVlans")) {
					String status = NetworkOps.AddDvPGVlans(si,adddvpgvlanList, adddvpgList,  datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddDvPGVlans : Success");
					} else {
						throw new Exception ("Error during AddDvPGVlans");
					}

				} else if (opCall.equals("AddStdVnic")) {
					String status = VMOps.AddStdVnic(si, deletevmMap, hostList, addpgList, dvswitchList, dvpgMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddStdVnic : Success");
					} else {
						throw new Exception ("Error during AddStdVnic");
					}
					
				} else if (opCall.equals("DeleteStdVnic")) {
					String status = VMOps.DeleteStdVnic(si, deletevmMap, hostList, addpgList, dvswitchList, dvpgMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeleteStdVnic : Success");
					} else {
						throw new Exception ("Error during DeleteStdVnic");
					}

				} else if (opCall.equals("AddDvnic")) {
					String status = VMOps.AddDvnic(si, deletevmMap, dvswitchList, dvpgMap, hostList, adddvpgList, adddvpgMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddDvnic : Success");
					} else {
						throw new Exception ("Error during AddDvnic");
					}
	
				} else if (opCall.equals("DeleteDvnic")) {
					String status = VMOps.DeleteDvnic(si, deletevmMap, dvswitchList, dvpgMap, hostList, adddvpgList, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeleteDvnic : Success");
					} else {
						throw new Exception ("Error during DeleteDvnic");
					}


				} else if (opCall.equals("AddStdVMks")) {
					String status = NetworkOps.AddStdVMks(si,hostList, vswitchList, addstdvmkMap, addpgMap,  datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddStdVMks : Success");
					} else {
						throw new Exception ("Error during AddStdVMks");
					}


				} else if (opCall.equals("DeleteStdVMks")) {
					String status = NetworkOps.DeleteStdVMks(si,hostList, vswitchList, addstdvmkMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeletePGs : Success");
					} else {
						throw new Exception ("Error during DeletePGs");
					}


				} else if (opCall.equals("ChangeStdVnics")) {
					String status = NetworkOps.AddPGs(si,hostList, changestdvnicList, changestdvnicMap, addpgvlanList, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("ChangeStdVnics: New StdPortGroup Creation : Success");
					} else {
						//throw new Exception ("Error during ChangeStdVnics-New StdPortGroup Creation");
						System.out.println("Error during ChangeStdVnics-New StdPortGroup Creation");
					}
//
//					status = NetworkOps.AddPGVlans(si, hostList, addpgvlanList, changestdvnicList, changestdvnicMap, datacenterList);
//					if (status.equals("SUCCESS")) {
//						PrintMsg.MsgPrint("ChangeStdVnics:  AddPGVlans : Success");
//					} else {
//						System.out.println("Error during ChangeStdVnics - AddPGVlans");
//					}
//
					status = NetworkOps.ChangeVnics(si,hostList, vmMap, addpgList, addpgMap, changestdvnicList, changestdvnicMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("ChangeStdVnics : Success");
					} else {
						throw new Exception ("Error during ChangeStdVnics");
					}

				} else if (opCall.equals("Rebuild")) {
					ArrayList<String> allPGs = new ArrayList<String>();
					allPGs.addAll(addpgList);
					allPGs.addAll(changestdvnicList);
					String status = NetworkOps.DeletePGs(si,hostList, allPGs, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("DeletePGs : Success");
					} else {
						throw new Exception ("Error during DeletePGs");
					}

					status = VMOps.DeleteVM(si, deletevmMap, hostList, dvswitchList, dvpgMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.PassPrint("DeleteVM : Success");
					} else {
						throw new Exception ("Error during DeleteVM");
					}

					status = NetworkOps.AddPGs(si,hostList, addpgList, addpgMap, datacenterList, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddPGs : Success");
					} else {
						throw new Exception ("Error during AddPGs");
					}

					status = VMOps.CreateVM(si, datacenterList, datastoreList, vmMap, stdvnicList, dvnicList, hostList, dvswitchList, dvpgMap, delayFlag);
					if (status.equals("SUCCESS")) {
						PrintMsg.PassPrint("CreateVM : Success");
					} else {
						throw new Exception ("Error during CreateVM");
					}

					status = VMOps.AddStdVnic(si, deletevmMap, hostList, addpgList, dvswitchList, dvpgMap, datacenterList);
					if (status.equals("SUCCESS")) {
						PrintMsg.MsgPrint("AddStdVnic : Success");
					} else {
						throw new Exception ("Error during AddStdVnic");
					}

				}
				si.getServerConnection().logout();

				} catch (Exception e) {
					si.getServerConnection().logout();
					PrintMsg.ErrorPrint("vCenter Commands Exception: " + e.getMessage()); 
				}
				
		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Attempting to make a connection to the vCenter");
		}

	}

	private static boolean ValidateInventory(String fileName) throws FileNotFoundException {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			int lineCount = 0;

			int savedHostListNum = 0;
			int addpgNum = 0;
			int vmListNum = 0;

			String line;
			while(( line = br.readLine()) != null) {
				String[] assetLine = line.split("\\s+");
				lineCount++;
				if(assetLine.length < 2) {
					if(assetLine.length == 1) {
						PrintMsg.ErrorPrint("Error In Line: " + lineCount + " - Invalid Asset Line. May Be An Empty Line: " + assetLine[0]);
						System.exit(-1);
					} else {
						PrintMsg.ErrorPrint("Please Delete the Empty Line");
						System.exit(-1);
					}
				}
				try {
					if (assetLine[0].equals("switchList")) {
						int switchNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != switchNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("switchList Number Doesn't Match The Number Of Listed Switches");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("vCenteripList")) {
						int vCenterNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != vCenterNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("vCenteripList Number Doesn't Match The Number Of Listed vCenters");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("vCenternameList")) {
						int vCenternameNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != vCenternameNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("vCenternameList Number Doesn't Match The Number Of Listed vCenters Names");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("unameList")) {
						int unameListNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != unameListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("unameList Number Doesn't Match The Number Of Listed UserNames");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("passwordList")) {
						int passwordListNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != passwordListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("passwordList Number Doesn't Match The Number Of Listed Passwords");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("datacenterList")) {
						int datacenterListNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != datacenterListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("datacenterList Number Doesn't Match The Number Of Listed DataCenters");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("hostList")) {
						int hostListNum = Integer.parseInt(assetLine[1]);
						if(assetLine.length != hostListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("hostList Number Doesn't Match The Number Of Listed Hosts");
							System.exit(-1);
						} else { 
							String[] assetList = Arrays.copyOfRange(assetLine, 2, assetLine.length);
							savedHostListNum = hostListNum;
							int hostListAttributes = 2;
							int loopCount = 0;
							int currentAssetNum = 0;
							while(( line = br.readLine()) != null) {
								lineCount++;
								String[] subAsset = line.split("\\s+");
								if(!subAsset[0].equals(assetList[currentAssetNum]))  {
									PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
//									PrintMsg.ErrorPrint("Invalid hostList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[loopCount/hostListAttributes]);
									PrintMsg.ErrorPrint("Invalid hostList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[currentAssetNum]);
									System.exit(-1);
								} else { 
									int hostassetNum = Integer.parseInt(subAsset[1]);
									if(subAsset.length != hostassetNum+2) {
										PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
										PrintMsg.ErrorPrint("Asset Number for Host: " +subAsset[0] + " Doesn't Match The Listed Assets");
										System.exit(-1);
									} else if(loopCount/hostListNum == 0) { 
										if(!subAsset[2].contains("vmnic")) {
											PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
											PrintMsg.ErrorPrint("Vmnic Name Expected for Host: " +subAsset[0] + " : Found - " + subAsset[2]);
											System.exit(-1);
										}
									} else if(loopCount/hostListNum == 1) {
										if(!subAsset[2].contains("/")) {
											PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
											PrintMsg.ErrorPrint("Interface Name Expected for Host: " +subAsset[0] + " : Found - " + subAsset[2]);
											System.exit(-1);
										}

									}

								}
								loopCount++;
								currentAssetNum++;
								if(loopCount == hostListNum*hostListAttributes) break;
								if(currentAssetNum == hostListNum) currentAssetNum = 0;
							}
						}

					} else if (assetLine[0].equals("datastoreList")) {
						int datastoreListNum = Integer.parseInt(assetLine[1]);
						if(datastoreListNum != savedHostListNum) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("datastoreList Number Doesn't Match The Number Of Hosts In hostList");
							System.exit(-1);
						}
						if(assetLine.length != datastoreListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("datastoreList Number Doesn't Match The Number Of Listed DataStores");
							System.exit(-1);
						}
				
					} else if (assetLine[0].equals("vswitchList")) {
						int vswitchListNum = Integer.parseInt(assetLine[1]);

						if (assetLine.length == 2) {
							globalNumvSwitches = vswitchListNum;

						} else if(assetLine.length != vswitchListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("vswitchList Number Doesn't Match The Number Of Listed vSwitches");
							System.exit(-1);

						} else { 
							String[] assetList = Arrays.copyOfRange(assetLine, 2, assetLine.length);
							int assetListAttributes = 3;
							int loopCount = 0;
							int currentAssetNum = 0;
							while(( line = br.readLine()) != null) {
								lineCount++;
								String[] subAsset = line.split("\\s+");
								if(!subAsset[0].equals(assetList[currentAssetNum]))  {
									PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
//									PrintMsg.ErrorPrint("Invalid vswitchList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[loopCount/assetListAttributes]);
									PrintMsg.ErrorPrint("Invalid vswitchList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[currentAssetNum]);
									System.exit(-1);
								} else { 
									int subAssetNum = Integer.parseInt(subAsset[1]);
									if (subAssetNum != 0) {
										if(subAsset.length != subAssetNum+2) {
											PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
											PrintMsg.ErrorPrint("Asset Number for vSwitch: " +subAsset[0] + " Doesn't Match The Listed Assets");
											System.exit(-1);

										} else if(loopCount/vswitchListNum == 0) { 
											if(!subAsset[2].contains("vmnic")) {
												PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
												PrintMsg.ErrorPrint("Vmnic Name Expected for vSwitch: " +subAsset[0] + " : Found - " + subAsset[2]);
												System.exit(-1);
											}
										}
									}
								}
								loopCount++;
								currentAssetNum++;
								if(loopCount == vswitchListNum*assetListAttributes) break;
								if(currentAssetNum == vswitchListNum) currentAssetNum = 0;
							}
						}

					} else if (assetLine[0].equals("dvswitchList")) {
						int dvswitchListNum = Integer.parseInt(assetLine[1]);

						if (assetLine.length == 2) {
							globalNumDvSwitches = dvswitchListNum;

						} else if(assetLine.length != dvswitchListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
							PrintMsg.ErrorPrint("dvswitchList Number Doesn't Match The Number Of Listed dvSwitches");
							System.exit(-1);

						} else { 
							String[] assetList = Arrays.copyOfRange(assetLine, 2, assetLine.length);
							int assetListAttributes = 4;
							int loopCount = 0;
							int currentAssetNum = 0;
							while(( line = br.readLine()) != null) {
								lineCount++;
								String[] subAsset = line.split("\\s+");
								if(!subAsset[0].equals(assetList[currentAssetNum]))  {
									PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
//									PrintMsg.ErrorPrint("Invalid dvswitchList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[loopCount/assetListAttributes]);
									PrintMsg.ErrorPrint("Invalid dvswitchList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[currentAssetNum]);
									System.exit(-1);
								} else { 
									int subAssetNum = Integer.parseInt(subAsset[1]);
									if (subAssetNum != 0) {
										if(subAsset.length != subAssetNum+2) {
											PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
											PrintMsg.ErrorPrint("Asset Number for dvSwitch: " +subAsset[0] + " Doesn't Match The Listed Assets");
											System.exit(-1);

										} else if(loopCount/dvswitchListNum == 0) { 
											if(!subAsset[2].contains("vmnic")) {
												PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
												PrintMsg.ErrorPrint("Vmnic Name Expected for dvSwitch: " +subAsset[0] + " : Found - " + subAsset[2]);
												System.exit(-1);
											}
										}
									}

								}
								loopCount++;
								currentAssetNum++;
								if(loopCount == dvswitchListNum*assetListAttributes) break;
								if(currentAssetNum == dvswitchListNum) currentAssetNum = 0;
							}
						}
						
					} else if (assetLine[0].equals("vmList")) {
						vmListNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length == 2) {
							globalNumVms = vmListNum;
						}else if(assetLine.length != vmListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("vmList Number Doesn't Match The Number Of Listed VMs");
							System.exit(-1);
						}
	
					} else if (assetLine[0].equals("stdvnicList")) {
						int stdvnicListNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length == 2) {
							globalNumStdVnics = stdvnicListNum;
						}else if(assetLine.length != stdvnicListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("stdvnicList Number Doesn't Match The Number Of Listed Standard VNICs");
							System.exit(-1);
						}

					} else if (assetLine[0].equals("dvnicList")) {
						int dvnicListNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length == 2) {
							globalNumDvnics = dvnicListNum;
						}else if(assetLine.length != dvnicListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("dvnicList Number Doesn't Match The Number Of Listed Distributed VNICs");
							System.exit(-1);
						}


					} else if (assetLine[0].equals("addpgList")) {
						addpgNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length == 2) { //AddPG doesn't specify the pgs name by name
							globalAddPGCountFlag = true ;
						} else if(assetLine.length != addpgNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
							PrintMsg.ErrorPrint("addpgList Number Doesn't Match The Number Of Listed Standard Port Groups");
							System.exit(-1);

						} else { 
							String[] assetList = Arrays.copyOfRange(assetLine, 2, assetLine.length);
							int assetListAttributes = 1;
							int loopCount = 0;
							int currentAssetNum = 0;
							while(( line = br.readLine()) != null) {
								lineCount++;
								String[] subAsset = line.split("\\s+");
								if(!subAsset[0].equals(assetList[currentAssetNum]))  {
									PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
//									PrintMsg.ErrorPrint("Invalid dvswitchList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[loopCount/assetListAttributes]);
									PrintMsg.ErrorPrint("Invalid addpgList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[currentAssetNum]);
									System.exit(-1);
								} else { 
									int subAssetNum = Integer.parseInt(subAsset[1]);
									if(subAsset.length != subAssetNum+2) {
										PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
										PrintMsg.ErrorPrint("Asset Number for PG: " +subAsset[0] + " Doesn't Match The Listed Assets");
										System.exit(-1);

									}
								}
								loopCount++;
								currentAssetNum++;
								if(loopCount == addpgNum*assetListAttributes) break;
								if(currentAssetNum == addpgNum) currentAssetNum = 0;
							}
						}

					} else if (assetLine[0].equals("adddvpgList")) {
						int adddvpgNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length == 2) { //AddDvPG doesn't specify the dvpgs name by name
							globalAddDvPGCountFlag = true ;

						} else if(assetLine.length != adddvpgNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
							PrintMsg.ErrorPrint("adddvpgList Number Doesn't Match The Number Of Listed Distributed Port Groups");
							System.exit(-1);

						} else { 
							String[] assetList = Arrays.copyOfRange(assetLine, 2, assetLine.length);
							int assetListAttributes = 1;
							int loopCount = 0;
							int currentAssetNum = 0;
							while(( line = br.readLine()) != null) {
								lineCount++;
								String[] subAsset = line.split("\\s+");
								if(!subAsset[0].equals(assetList[currentAssetNum]))  {
									PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
									PrintMsg.ErrorPrint("Invalid adddvpgList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[currentAssetNum]);
									System.exit(-1);
								} else { 
									int subAssetNum = Integer.parseInt(subAsset[1]);
									if(subAsset.length != subAssetNum+2) {
										PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
										PrintMsg.ErrorPrint("Asset Number for DvPG: " +subAsset[0] + " Doesn't Match The Listed Assets");
										System.exit(-1);

									}
								}
								loopCount++;
								currentAssetNum++;
								if(loopCount == adddvpgNum*assetListAttributes) break;
								if(currentAssetNum == adddvpgNum) currentAssetNum = 0;
							}
						}

					} else if (assetLine[0].equals("addpgvlanList")) {
						int pgvlanListNum = Integer.parseInt(assetLine[1]);
//
//						if(pgvlanListNum != 1) {
//							if(pgvlanListNum != addpgNum) {
//								PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
//								PrintMsg.ErrorPrint("addpgvlanList Number Should Be Equal To 1 or AddPGListNumber");
//								System.exit(-1);
//							}
//						}


						if(assetLine.length != pgvlanListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("addpgvlanList Number Doesn't Match The Number Of VLANs");
							System.exit(-1);
						}


					} else if (assetLine[0].equals("adddvpgvlanList")) {
						int dvpgvlanListNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length != dvpgvlanListNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("adddvpgvlanList Number Doesn't Match The Number Of VLANs");
							System.exit(-1);
						}
						
					} else if (assetLine[0].equals("addstdvmkList")) {
						int vmkListNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length != 2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("Only One Number Is Permited For stdvmList");
							System.exit(-1);
						}
	
					} else if (assetLine[0].equals("deletehostList")) {
						int deleteHostNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length != deleteHostNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("deletehostList Number Doesn't Match The Number Of Hosts");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("deletevssList")) {
						int deleteVssNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length != deleteVssNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("deletevssList Number Doesn't Match The Number Of vSwitches");
							System.exit(-1);
						}
					} else if (assetLine[0].equals("deletedvsList")) {
						int deleteDvsNum = Integer.parseInt(assetLine[1]);

						if(assetLine.length != deleteDvsNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
							PrintMsg.ErrorPrint("deleteDvsList Number Doesn't Match The Number Of dvSwitches");
							System.exit(-1);
						}

					} else if (assetLine[0].equals("changestdvnicList")) {
						int changestdvnicNum = Integer.parseInt(assetLine[1]);

						if(changestdvnicNum != 1) {
							if(changestdvnicNum != vmListNum) {
								PrintMsg.MsgPrint("Error In Line: " +lineCount + "  - \"" + line + " \"");
								PrintMsg.ErrorPrint("changestdvnicList Number Should Be Equal To 1 or vmListNum");
								System.exit(-1);
							}
						}

						if(assetLine.length != changestdvnicNum+2) {
							PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
							PrintMsg.ErrorPrint("changestdvnicList Number Doesn't Match The Number Of Listed Standard Port Groups");
							System.exit(-1);

						} else { 
							String[] assetList = Arrays.copyOfRange(assetLine, 2, assetLine.length);
							int assetListAttributes = 1;
							int loopCount = 0;
							int currentAssetNum = 0;
							while(( line = br.readLine()) != null) {
								lineCount++;
								String[] subAsset = line.split("\\s+");
								if(!subAsset[0].equals(assetList[currentAssetNum]))  {
									PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
									PrintMsg.ErrorPrint("Invalid changestdvnicList Asset: " + subAsset[0] + " - Expecting Asset: " +assetList[currentAssetNum]);
									System.exit(-1);
								} else { 
									int subAssetNum = Integer.parseInt(subAsset[1]);
									if(subAsset.length != subAssetNum+2) {
										PrintMsg.MsgPrint("Error In Line: " +lineCount + " - \"" + line + " \"");
										PrintMsg.ErrorPrint("Asset Number for changestdvnicList: " +subAsset[0] + " Doesn't Match The Listed Assets");
										System.exit(-1);

									}
								}
								loopCount++;
								currentAssetNum++;
								if(loopCount == changestdvnicNum*assetListAttributes) break;
								if(currentAssetNum == changestdvnicNum) currentAssetNum = 0;
							}
						}
					}






				//	

				//		if(vCenterNum != 1) {
				//			PrintMsg.ErrorPrint("Supported vCenteripList Number is : 1");
				//			System.exit(-1);
				//		}
	


				} catch (NumberFormatException e) {
					PrintMsg.ErrorPrint("Invalid Asset Number " + e);
				}
			}
		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Validating Inventory File: " + fileName + " - " + e);
			System.exit(0);
		}
		return true;



	}

	private static void ReadInventory(String fileName) throws FileNotFoundException { 

		Scanner input = new Scanner(new BufferedReader(new FileReader(fileName)));
		while(input.hasNext()) {
			String asset = input.next();
			if(asset.equals("switchList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					switchList.add(input.next());
					len++;
				}

			}else if(asset.equals("vCenteripList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					vCenteripList.add(input.next());
					len++;
				}

			}else if(asset.equals("unameList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					unameList.add(input.next());
					len++;
				}

			}else if(asset.equals("passwordList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					passwordList.add(input.next());
					len++;
				}

			}else if(asset.equals("vCenternameList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					vCenternameList.add(input.next());
					len++;
				}
				
			}else if(asset.equals("datacenterList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					datacenterList.add(input.next());
					len++;
				}
	
			}else if(asset.equals("vmList")) {
				int listLen = input.nextInt();
				int len;
				int curvmNum = 1;

				for (int i=0; i<hostList.size(); i++) {
					len = 0;
					ArrayList<String> hostvmList = new ArrayList<String>();
					ArrayList<String> hostclonevmList = new ArrayList<String>();
					ArrayList<String> hostrenamevmList = new ArrayList<String>();
					ArrayList<String> hostdeletevmList = new ArrayList<String>();
					while(len < listLen) {
						String curvmName = "v"+ Integer.toString(curvmNum);
						hostvmList.add(curvmName);
						hostdeletevmList.add(curvmName);
						String curclonevmName = "c"+ Integer.toString(curvmNum);
						hostclonevmList.add(curclonevmName);
						hostdeletevmList.add(curclonevmName);
						String currenamevmName = "r"+ Integer.toString(curvmNum);
						hostrenamevmList.add(currenamevmName);
						hostdeletevmList.add(currenamevmName);
						len++;
						curvmNum++;
					}
					vmMap.put(hostList.get(i), hostvmList);
					clonevmMap.put(hostList.get(i), hostclonevmList);
					renamevmMap.put(hostList.get(i), hostrenamevmList);
					deletevmMap.put(hostList.get(i), hostdeletevmList);
				}

			}else if(asset.equals("datastoreList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					datastoreList.add(input.next());
					len++;
				}

			}else if(asset.equals("stdvnicList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					stdvnicList.add(input.next());
					len++;
				}

			}else if(asset.equals("dvnicList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					dvnicList.add(input.next());
					len++;
				}

			}else if(asset.equals("hostList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					hostList.add(input.next());
					len++;
				}
				len = 0;
				//Build the hostvmnicMap hash map for the hosts
				while(len < listLen) {
					String curHost = input.next();
					int numVmnics = input.nextInt();
					int j=0;
					ArrayList<String> vmnicList = new ArrayList<String>();
					while(j < numVmnics) {
						vmnicList.add(input.next());
						j++;
					}
					hostvmnicMap.put(curHost, vmnicList);
					len++;
				}
				len = 0;
				//Build the hostInterfaceMap hash map for the hosts
				while(len < listLen) {
					String curHost = input.next();
					int numInterfaces = input.nextInt();
					int j=0;
					ArrayList<String> interfaceList = new ArrayList<String>();
					while(j < numInterfaces) {
						interfaceList.add(input.next());
						j++;
					}
					hostInterfaceMap.put(curHost, interfaceList);
					len++;
				}

			}else if(asset.equals("vswitchList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					//String next = input.next();
					//System.out.println("next is: " + next);
					vswitchList.add(input.next());
					len++;
				}
				len = 0;
				//Build the pguplinkMap hash map for the vSwitches
				while(len < listLen) {
					String curSwitch = input.next();
					int numUplinks = input.nextInt();
					int j=0;
					ArrayList<String> uplinkList = new ArrayList<String>();
					while(j < numUplinks) {
						uplinkList.add(input.next());
						j++;
					}
					pguplinkMap.put(curSwitch, uplinkList);
					len++;
				}
				//Build the pgMap hash map for the vSwitches	
				len = 0;
				while(len < listLen) {
					String curSwitch = input.next();
					int numPGs = input.nextInt();
					int j=0;
					ArrayList<String> pgList = new ArrayList<String>();
					while (j < numPGs) {
						pgList.add(input.next());
						j++;
					}
					pgMap.put(curSwitch, pgList);
					len++;
				}

				//Build the pgvlanMap hash map for the vSwitches
				len = 0;
				while(len < listLen) {
					String curSwitch = input.next();
					int numVlans = input.nextInt();
					int j=0;
					ArrayList<Integer> vlanList = new ArrayList<Integer>();
					while (j < numVlans) {
						vlanList.add(input.nextInt());
						j++;
					}
					pgvlanMap.put(curSwitch, vlanList);
					len++;
				}

			}else if(asset.equals("deletevssList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					deletevssList.add(input.next());
					len++;
				}

			}else if(asset.equals("dvswitchList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					dvswitchList.add(input.next());
					len++;
				}
				len = 0;
				//Build the dvpguplinkMap hash map for the dvSwitches
				while(len < listLen) {
					String curSwitch = input.next();
					int numUplinks = input.nextInt();
					int j=0;
					ArrayList<String> uplinkList = new ArrayList<String>();
					while(j < numUplinks) {
						uplinkList.add(input.next());
						j++;
					}
					dvpguplinkMap.put(curSwitch, uplinkList);
					len++;
				}
				//Build the dvpgMap hash map for the dvSwitches	
				len = 0;
				while(len < listLen) {
					String curSwitch = input.next();
					int numPGs = input.nextInt();
					int j=0;
					ArrayList<String> pgList = new ArrayList<String>();
					while (j < numPGs) {
						pgList.add(input.next());
						j++;
					}
					dvpgMap.put(curSwitch, pgList);
					len++;
				}

				//Build the dvpgvlanMap hash map for the dvSwitches
				len = 0;
				while(len < listLen) {
					String curSwitch = input.next();
					int numVlans = input.nextInt();
					int j=0;
					ArrayList<Integer> vlanList = new ArrayList<Integer>();
					while (j < numVlans) {
						vlanList.add(input.nextInt());
						j++;
					}
					dvpgvlanMap.put(curSwitch, vlanList);
					len++;
				}
				//Build the ldpMap hash map for the dvSwitches	
				len = 0;
				while(len < listLen) {
					String curSwitch = input.next();
					int numPGs = input.nextInt();
					int j=0;
					ArrayList<String> ldpList = new ArrayList<String>();
					while (j < numPGs) {
						ldpList.add(input.next());
						j++;
					}
					dvsldpMap.put(curSwitch, ldpList);
					len++;
				}


			}else if(asset.equals("deletedvsList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					deletedvsList.add(input.next());
					len++;
				}

			}else if(asset.equals("deletehostList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					deletehostList.add(input.next());
					len++;
				}


			}else if(asset.equals("addpgList")) {
				int listLen = input.nextInt();
				int len = 0;
				
				if(globalAddPGCountFlag) { //If the inventory file only have the AddPG count
					int curpgNum = 1;
					while(len <listLen) {
						String curpgName = "pg" + Integer.toString(curpgNum);
						addpgList.add(curpgName);
						len++;
						curpgNum++;
					} 
					String curPG = input.next(); //read the next input. We are not going to use this though. Just want to move the needle.
					int numvSwitches = input.nextInt(); //read the next input. We are not going to use this though. Just want to move the needle.
					String curVswitch = input.next();
					
					ArrayList<String> vSwitchList = new ArrayList<String>();
					vSwitchList.add(curVswitch);
					for(int j=0; j<addpgList.size(); j++) {
						addpgMap.put(addpgList.get(j), vSwitchList);
					}


				} else { //Inventory file explicitely gives out the AddPG name
					
					while(len < listLen) {
						addpgList.add(input.next());
						len++;
					}
					len = 0;
					//Build the addpgMap hash map for the pgs
					while(len < listLen) {
						String curPG = input.next();
						int numvSwitches = input.nextInt();
						int j=0;
						ArrayList<String> vSwitchList = new ArrayList<String>();
						while(j < numvSwitches) {
							vSwitchList.add(input.next());
							j++;
						}
						addpgMap.put(curPG, vSwitchList);
						len++;
					}
				}

			}else if(asset.equals("adddvpgList")) {
				int listLen = input.nextInt();
				int len = 0;

				if(globalAddDvPGCountFlag) { //If the inventory file only have the AddDvPG count
					int curdvpgNum = 1;
					while(len <listLen) {
						String curdvpgName = "dvpg" + Integer.toString(curdvpgNum);
						adddvpgList.add(curdvpgName);
						len++;
						curdvpgNum++;
					} 
					String curdvPG = input.next(); //read the next input. We are not going to use this though. Just want to move the needle.
					int numdvSwitches = input.nextInt(); //read the next input. We are not going to use this though. Just want to move the needle.
					String curDVswitch = input.next();
					
					ArrayList<String> dvSwitchList = new ArrayList<String>();
					dvSwitchList.add(curDVswitch);
					for(int j=0; j<adddvpgList.size(); j++) {
						adddvpgMap.put(adddvpgList.get(j), dvSwitchList);
					}


				} else { //Inventory file explicitely gives out the AddDvPG name

					while(len < listLen) {
						adddvpgList.add(input.next());
						len++;
					}
					len = 0;
					//Build the adddvpgMap hash map for the dvpgs
					while(len < listLen) {
						String curdvPG = input.next();
						int numdvSwitches = input.nextInt();
						int j=0;
						ArrayList<String> dvSwitchList = new ArrayList<String>();
						while(j < numdvSwitches) {
							dvSwitchList.add(input.next());
							j++;
						}
						adddvpgMap.put(curdvPG, dvSwitchList);
						len++;
					}
				}

			}else if(asset.equals("addpgvlanList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					addpgvlanList.add(input.nextInt());
					len++;
				}

			}else if(asset.equals("adddvpgvlanList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					adddvpgvlanList.add(input.next());
					len++;
				}

			}else if(asset.equals("addstdvmkList")) {
				int listLen = input.nextInt();
				int len;
				for (int i=0; i<hostList.size(); i++) {
					int curvmkNum = 101;
					for (int j=0; j<vswitchList.size(); j++) {
						len = 0;
						ArrayList<String> vmkList = new ArrayList<String>();
						while(len < listLen) {
							String curvmkName = "VMkernel" + Integer.toString(curvmkNum);
							vmkList.add(curvmkName);
							len++;
							curvmkNum++;
						}
						addstdvmkMap.put(hostList.get(i).concat(vswitchList.get(j)), vmkList);
					}
				}

			}else if(asset.equals("changestdvnicList")) {
				int listLen = input.nextInt();
				int len = 0;
				while(len < listLen) {
					changestdvnicList.add(input.next());
					len++;
				}
				len = 0;
				//Build the changestdvnicMap hash map for the pgs
				while(len < listLen) {
					String curVnic = input.next();
					int numvSwitches = input.nextInt();
					int j=0;
					ArrayList<String> vSwitchList = new ArrayList<String>();
					while(j < numvSwitches) {
						vSwitchList.add(input.next());
						j++;
					}
					changestdvnicMap.put(curVnic, vSwitchList);
					len++;
				}
			}


		}

		input.close();
	}

	
	private static BufferedWriter writeFile(String fileName) throws Exception {
		try { 
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			return bw;

		} catch (Exception e) {
			PrintMsg.ErrorPrint("Error Creating File: " + fileName + " - " + e);
			System.exit(0);
		}

		return null;	


	}

	private static void DiscoverVcenter(ServiceInstance si, String dcName) throws Exception { 

		try {
			Folder rootFolder = si.getRootFolder();
			Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", datacenterList.get(0));
			Folder hostFolder = dc.getHostFolder();
			ManagedEntity[] hostArray = new InventoryNavigator(hostFolder).searchManagedEntities("HostSystem");

			for(int i=0; i<hostArray.length; i++) {
				System.out.println("Host is: " + hostArray[i].getName());
				HostSystem host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostArray[i].getName());
				PhysicalNic[] pnics = host.getConfig().getNetwork().getPnic();
				for(int j=0; j<pnics.length; j++) {
					System.out.println("Pnics.key is: " + pnics[j].getDevice());
				}

				HostVirtualSwitch[] vswitches = host.getConfig().getNetwork().getVswitch();
				for(int k=0; k<vswitches.length; k++) {
					System.out.println("vswitch is: " + vswitches[k].getName());
					String uplinks[] = vswitches[k].getPnic();
					for(int m=0; m<uplinks.length; m++) {
						System.out.println("vmnics are: " + uplinks[m]);
					}
				}


			}
		}catch (Exception e) {
			PrintMsg.ErrorPrint("Error In DiscoverVcenter: DataCenter: "+dcName  + "--" + e);
			System.exit(0);
		}


	}

}
