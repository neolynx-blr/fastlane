package com.neolynks.worker.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.neolynks.worker.exception.WorkerException;
import com.neolynks.worker.exception.WorkerException.WORKER_SESSION_ERROR;
import com.neolynks.worker.dto.WorkerCart;
import com.neolynks.worker.dto.WorkerSession;
import com.neolynks.worker.dto.WorkerTask;

/**
 * 
 * @author abhishekshukla
 * Right now Workercart and WorkerSession operations both are 
 * handler by this class. If it makes sense in future we should 
 * create two manager classes to handle operations separately
 */
public class WorkerSessionHandler {

	private ConcurrentHashMap<Long, Set<WorkerSession>> storeIdToWorkerSessionsMap;
	private ConcurrentHashMap<Long, WorkerSession> idToWorkerSessionMap;
	private ConcurrentLinkedQueue<WorkerCart> unassignedWorkerCartQueue ;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private volatile boolean isShutDown = false;
	
	public WorkerSessionHandler() {
		storeIdToWorkerSessionsMap = new ConcurrentHashMap<Long, Set<WorkerSession>>();
		idToWorkerSessionMap = new ConcurrentHashMap<Long, WorkerSession>();
		unassignedWorkerCartQueue = new ConcurrentLinkedQueue<WorkerCart>();
		executorService.execute(new WorkerCartProcessor());
	}

	public void shutDown() {
		isShutDown = true;
		executorService.shutdown();
	}

	private class WorkerCartProcessor implements Runnable
    {  	 
        public void run()
        {
     	   while(!isShutDown)
     	   {
        	try 
        	{
        		WorkerCart workerCart = null;
        		if (null != (workerCart = unassignedWorkerCartQueue.peek())) {
        			WorkerSession workerSession = getMostIdleWorkerSession(workerCart.getStoreId());
        			if (null != workerSession) {
            			workerSession.addWorkerCart(workerCart);
        			}
        			unassignedWorkerCartQueue.poll();
        		}
        	} 
        	catch(Exception e)
        	{
        		// log exception and ignore.
        	}
    	   }
        }
    }

	public void addWorkerCartForWorkerSessionAssignment(WorkerCart workerCart) {
		unassignedWorkerCartQueue.add(workerCart);
	}

	/**
	 * 
	 * @param storeId
	 * @return
	 * Function can throw concurrent modification exception as we are iterating a synchronized set.
	 * We would like to deal with it without using locks in favour of concurrency.
	 * Caller should re-call method to getMostIdleWorkerSession
	 */
	private WorkerSession getMostIdleWorkerSession(long storeId) {
		Set<WorkerSession> workerSessions = storeIdToWorkerSessionsMap.get(storeId);
		if (null == workerSessions || 0 == workerSessions.size()) {
			return null;
		}

		WorkerSession selectedWorkerSession = null;
		for (WorkerSession workerSession: workerSessions) {
			if (workerSession.isOpen() && !workerSession.isOverLoaded() 
					&& (null == selectedWorkerSession || selectedWorkerSession.getLoad(false) > workerSession.getLoad(false))) {
				selectedWorkerSession = workerSession;
			}
		}
		return selectedWorkerSession;
	}

	private WorkerSession getWorkerSessionForId(long workerSessionId) {
		WorkerSession workerSession = idToWorkerSessionMap.get(workerSessionId);
		if (null == workerSession) {
			throw new WorkerException(WORKER_SESSION_ERROR.UNKNOWN_SESSION_ID);
		}
		return workerSession;
	}

	public void addWorkerSession(WorkerSession workerSession) {
		storeIdToWorkerSessionsMap.putIfAbsent(workerSession.getWorker().getStoreId(), Collections.synchronizedSet(new HashSet<WorkerSession>()));
		idToWorkerSessionMap.putIfAbsent(workerSession.getId(), workerSession);
		Set<WorkerSession> workerSessions = storeIdToWorkerSessionsMap.get(workerSession.getWorker().getStoreId());
		workerSessions.add(workerSession);
	}

	public void pauseWorkerSession(long workerSesssionId) {
		WorkerSession workerSession = getWorkerSessionForId(workerSesssionId);
		workerSession.pause();
	}

	public void terminateWorkerSession(long workerSesssionId) {
		WorkerSession workerSesssion = getWorkerSessionForId(workerSesssionId);
		unassignedWorkerCartQueue.addAll(workerSesssion.closeAndReleaseWorkerCarts());
		storeIdToWorkerSessionsMap.get(workerSesssion.getWorker().getStoreId()).remove(workerSesssion);
		idToWorkerSessionMap.remove(workerSesssionId);
	}

	public void reactivateWorkerSession(long workerSesssionId) {
		WorkerSession workerSession = getWorkerSessionForId(workerSesssionId);
		workerSession.restart();
	}

	public WorkerTask getWorkerTaskDetails(long workerSessionId) {
		WorkerSession workerSession = getWorkerSessionForId(workerSessionId);
		WorkerTask workerTask = workerSession.getWorkerTask();
		return workerTask;
	}

	public void completeWorkerTask(long workerSessionId, long workerTaskId) {
		WorkerSession workerSession = getWorkerSessionForId(workerSessionId);
		workerSession.completeWorkerTask(workerTaskId);
	}
}
