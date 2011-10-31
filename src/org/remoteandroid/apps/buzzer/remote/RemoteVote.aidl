package org.remoteandroid.apps.buzzer.remote;
interface RemoteVote 
{
	void standby();
	int vote (int position, long startTime,int temps);
}