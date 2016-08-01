package com.shiro.helloworld;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple Quickstart application showing how to use Shiro's API.
 */
public class Quickstart {

    private static final transient Logger log = LoggerFactory.getLogger(Quickstart.class);


    public static void main(String[] args) {

        // The easiest way to create a Shiro SecurityManager with configured
        // realms, users, roles and permissions is to use the simple INI config.
        // We'll do that by using a factory that can ingest a .ini file and
        // return a SecurityManager instance:

        // Use the shiro.ini file at the root of the classpath
        // (file: and url: prefixes load from files and urls respectively):
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();

        // for this simple example quickstart, make the SecurityManager
        // accessible as a JVM singleton.  Most applications wouldn't do this
        // and instead rely on their container configuration or web.xml for
        // webapps.  That is outside the scope of this simple quickstart, so
        // we'll just do the bare minimum so you can continue to get a feel
        // for things.
        SecurityUtils.setSecurityManager(securityManager);

        // Now that a simple Shiro environment is set up, let's see what you can do:

        // get the currently executing user:
        // 1. 获取当前的 Subject(和 Shiro 交互的对象), 调用 SecurityUtils.getSubject() 方法. 
        Subject currentUser = SecurityUtils.getSubject();

        // Do some stuff with a Session (no need for a web or EJB container!!!)
        // 了解: 测试使用 Session. 
        Session session = currentUser.getSession();
        session.setAttribute("someKey", "aValue");
        String value = (String) session.getAttribute("someKey");
        if (value.equals("aValue")) {
            log.info("--> Retrieved the correct value! [" + value + "]");
        }

        // let's login the current user so we can check against roles and permissions:
        //2. 检测当前用户是否已经被认证. 即是否登录. 调用 Subject 的 isAuthenticated() 方法. 
        if (!currentUser.isAuthenticated()) {
        	//3. 把用户名和密码封装为一个 UsernamePasswordToken 对象. 
            UsernamePasswordToken token = new UsernamePasswordToken("lonestarr", "vespa");
            token.setRememberMe(true);
            try {
            	//4. 执行登录. 调用 Subject 的 login(AuthenticationToken). 通常传入的是 UsernamePasswordToken 对象
            	//这也说明 UsernamePasswordToken 是 AuthenticationToken 的实现类. 
                currentUser.login(token);
            } 
            //5. 若用户名不存在, 则会抛出 UnknownAccountException 异常. 
            catch (UnknownAccountException uae) {
                log.info("--> There is no user with username of " + token.getPrincipal());
                return;
            } 
            //6. 若用户名存在, 但密码不匹配, 则会抛出 IncorrectCredentialsException 异常. 
            catch (IncorrectCredentialsException ice) {
                log.info("--> Password for account " + token.getPrincipal() + " was incorrect!");
                return; 
            } 
            //7. 若用户被锁定, 则会抛出 LockedAccountException 异常. 该异常需要在 Realm 的实现类中手工抛出.
            //而系统不会抛出. 
            catch (LockedAccountException lae) {
                log.info("The account for username " + token.getPrincipal() + " is locked.  " +
                        "Please contact your administrator to unlock it.");
            }
            // ... catch more exceptions here (maybe custom ones specific to your application?
            //8. 认证时所有异常的父类. 即前面的异常都是该异常的子类. 
            catch (AuthenticationException ae) {
                //unexpected condition?  error?
            }
        }

        //say who they are:
        //print their identifying principal (in this case, a username):
        log.info("--> User [" + currentUser.getPrincipal() + "] logged in successfully.");

        //test a role:
        //9. 测试登录的用户是否具有某一个指定的权限。 实际开发时通常不这样做. 
        if (currentUser.hasRole("schwartz")) {
            log.info("--> May the Schwartz be with you!");
        } else {
            log.info("Hello, mere mortal.");
        }

        //test a typed permission (not instance-level)
        //10. 测试登录的用户是否能完成某一个具体的行为
        // lightsaber:* 意为可以对 lightsaber 的任何对象做任何操作. 
        if (currentUser.isPermitted("lightsaber:weild")) {
            log.info("--> You may use a lightsaber ring.  Use it wisely.");
        } else {
            log.info("Sorry, lightsaber rings are for schwartz masters only.");
        }

        //a (very powerful) Instance Level permission:
        //11. user:query:zhangsan 意为可以对 user 的 zhangsan 实例做 query 的操作. 
        if (currentUser.isPermitted("user:query:zhangsan")) {
            log.info("--> You are permitted to 'drive' the winnebago with license plate (id) 'eagle5'.  " +
                    "Here are the keys - have fun!");
        } else {
            log.info("Sorry, you aren't allowed to drive the 'eagle5' winnebago!");
        }

        //all done - log out!
        //12. 登出. 
        currentUser.logout();

        System.exit(0);
    }
}
