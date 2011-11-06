/*
Copyright (c) 2011, Marcos Diez --  marcos AT unitron.com.br
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.   
 * Neither the name of  Marcos Diez nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.MarcosDiez.shareviahttp;

//package webs;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.net.Uri;
import android.util.Log;

/**
 * 
 * Title: A simple Webserver Tutorial NO warranty, NO guarantee, MAY DO damage
 * to FILES, SOFTWARE, HARDWARE!! Description: This is a simple tutorial on
 * making a webserver posted on http://turtlemeat.com . Go there to read the
 * tutorial! This program and sourcecode is free for all, and you can copy and
 * modify it as you like, but you should give credit and maybe a link to
 * turtlemeat.com, you know R-E-S-P-E-C-T. You gotta respect the work that has
 * been put down.
 * 
 * Copyright: Copyright (c) 2002 Company: TurtleMeat
 * 
 * @author: Jon Berg <jon.berg[on_server]turtlemeat.com
 * @version 1.0
 */

// file: server.java
// the real (http) serverclass
// it extends thread so the server is run in a different
// thread than the gui, that is to make it responsive.
// it's really just a macho coding thing.
public class MyHttpServer extends Thread {

	// by desing, we can only serve one file at a time.

	public static void SetFile(Uri fileUri) {
		MyHttpServer.fileUri = fileUri;
	}

	public static void SetFile(String path) {
		MyHttpServer.fileUri = Uri.parse(path);
	}

	// the constructor method
	// the parameters it takes is what port to bind to, the default tcp port
	// for a httpserver is port 80. the other parameter is a reference to
	// the gui, this is to pass messages to our nice interface
	public MyHttpServer(int listen_port) {
		port = listen_port;
		if (serversocket == null) {
			this.start();
		}
	}

	private static int port; // port we are going to listen to
	private static Uri fileUri;

	private static ServerSocket serversocket = null;
	private boolean webserverLoop = true;

	public String getServerUrl() {
		if (port == 80) {
			return "http://" + getLocalIpAddress() + "/";
		}
		return "http://" + getLocalIpAddress() + ":" + port + "/";
	}

	public void stopServer() {
		s("Closing server...\n\n");
		webserverLoop = false;
		if (serversocket != null) {
			try {
				serversocket.close();
				serversocket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	public String getLocalIpAddress() {
		try {
			InetAddress localAddress = null;
			InetAddress ipv6 = null;

			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (inetAddress instanceof Inet6Address) {
						ipv6 = inetAddress;
						continue;
					}
					if (inetAddress.isLoopbackAddress()) {
						localAddress = inetAddress;
						continue;
					}
					return inetAddress.getHostAddress().toString();
				}
			}
			if (ipv6 != null) {
				return ipv6.getHostAddress().toString();
			}
			if (localAddress != null) {
				return localAddress.getHostAddress().toString();
			}
			return "0.0.0.0";

		} catch (SocketException ex) {
			Log.e("httpServer", ex.toString());
		}
		return "0.0.0.0";
	}

	private boolean normalBind(int thePort) {
		s("Attempting to bind on port " + thePort);
		try {
			serversocket = new ServerSocket(thePort);
		} catch (Exception e) {
			s("Fatal Error:" + e.getMessage() + " " + e.getClass().toString());
			return false;
		}
		port = thePort;
		s("Binding was OK!");
		return true;
	}

	/*
	 * // TODO.... // ./iptables_armv5 -t nat -A PREROUTING -p tcp -m tcp --dport 80 -j REDIRECT --to-ports 9999 // so we could "bind" port 80 on a
	 * rooted android device...
	 * 
	 * public boolean rootBind() { boolean retval = false; Process suProcess;
	 * 
	 * try { suProcess = Runtime.getRuntime().exec("su");
	 * 
	 * DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
	 * DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
	 * 
	 * if (null != os && null != osRes) { // Getting the id of the current user
	 * to check if this is root os.writeBytes("id\n"); os.flush();
	 * 
	 * String currUid = osRes.readLine(); boolean exitSu = false; if (null ==
	 * currUid) { retval = false; exitSu = false; Log.d("ROOT",
	 * "Can't get root access or denied by user"); } else if (true ==
	 * currUid.contains("uid=0")) { // retval = true; exitSu = true;
	 * Log.d("ROOT", "Root access granted"); retval = normalBind(80); } else {
	 * retval = false; exitSu = true; Log.d("ROOT", "Root access rejected: " +
	 * currUid); }
	 * 
	 * if (exitSu) { os.writeBytes("exit\n"); os.flush(); } } } catch (Exception
	 * e) { // Can't get root ! // Probably broken pipe exception on trying to
	 * write to output // stream after su failed, meaning that the device is not
	 * rooted
	 * 
	 * retval = false; Log.d("ROOT", "Root access rejected [" +
	 * e.getClass().getName() + "] : " + e.getMessage()); }
	 * 
	 * return retval; }
	 */

	public void run() {
		s("Starting " + Util.myLogName + " server v" + Util.getAppVersion());
		if (!normalBind(port)) {
			return;
		}
		s("Bound on " + getServerUrl() + "\n");

		// go in a infinite loop, wait for connections, process request, send
		// response
		while (webserverLoop) {
			s("Ready, Waiting for requests...\n");

			// this call waits/blocks until someone connects to the port we
			// are listening to

			try {
				Socket connectionsocket = serversocket.accept();
				new HttpServerConnection(fileUri, connectionsocket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} // go back in loop, wait for next request
	}

	private void s(String s2) { // an alias to avoid typing so much!
		Log.d(Util.myLogName, s2);
	}

} // class phhew caffeine yes please!
