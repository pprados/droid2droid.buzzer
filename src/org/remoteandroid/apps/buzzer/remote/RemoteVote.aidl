package org.remoteandroid.apps.buzzer.remote;
interface RemoteVote 
{
	void exit();
	void standby();
	int vote (int position, long startTime,int temps);
}