package woyou.aidlservice.jiuiv5;

/**
 * Callback of the execution result of the print service
 */
interface ICallback {

	/**
	* Return the result of interface execution
	* Note: This callback only indicates whether the interface execution is successful but does not indicate the printer's work results. If you need to get the printer results, please use transaction mode
	* @param isSuccess: true execution succeeded, false execution failed
	*/
	oneway void onRunResult(boolean isSuccess);

	/**
	* Return the result of interface execution (string data)
	* @param result: As a result, the length of printing since the printer was powered on (unit: mm)
	*/
	oneway void onReturnString(String result);

	/**
	* Return the specific reason for the abnormal situation when the interface execution fails
	* code: exception code
	* msg: exception description
	*/
	oneway void  onRaiseException(int code, String msg);

	/**
	* Return the printer result
	* code: exception code 0 success 1 failure
	* msg: exception description
	*/
	oneway void  onPrintResult(int code, String msg);
	
}