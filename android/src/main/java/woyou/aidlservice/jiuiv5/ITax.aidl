package woyou.aidlservice.jiuiv5;

/**
 * Callback of the execution result of the print service
 */
interface ITax {

	oneway void onDataResult(in byte [] data);
	
}