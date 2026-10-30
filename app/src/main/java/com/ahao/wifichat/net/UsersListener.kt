package com.ahao.wifichat.net;


import com.ahao.wifichat.entity.Userinfo;

/**
 * 用户上下线相关回调
 * @author hao.ruan
 *
 */
public interface UsersListener {

	/**
	 * 用户上线了
	 * 
	 * @param user
	 */
	void userAdded(Userinfo user);

	/**
	 * 用户突发状况
	 * 
	 * @param user
	 */
	void userChanged(Userinfo user);

	/**
	 * 用户下线了
	 * 
	 * @param user
	 */
	void userRemoved(Userinfo user);

}
