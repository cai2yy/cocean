package app.armot.api.controller;

import annotations.Inject;
import app.armot.api.bean.User;
import app.armot.api.service.UserServer;
import app.armot.mock.UserDB;
import app.armot.utils.Session;
import app.armot.server.Controller;
import app.armot.server.HttpContext;
import app.armot.server.HttpRequest;
import app.armot.server.Router;

import java.util.UUID;

/**
 * @author Cai2yy
 * @date 2020/2/21 18:06
 */

public class UserController implements Controller {

    @Inject
    UserServer userServer;

    @Inject
    Session session;

    @Inject
    UserDB userDB;

    @Override
    public Router route() {
        return new Router()
                .handler("/login", "GET", this::getLoginPage)
                .handler("/login", "POST", this::login);
    }

    public void getLoginPage(HttpContext ctx, HttpRequest req) {
        ctx.render("login.ftl");
    }

    public void login(HttpContext ctx, HttpRequest req) {
        String username = req.mixedParam("username");
        String password = req.mixedParam("password");

        if (!userDB.checkAccess(username, password)) {
            ctx.abort(401, "用户名密码错误");
        }

        User user = new User(username);
        String sid = UUID.randomUUID().toString();
        session.setUser(sid, user);
        ctx.redirect("/user");

    }


}
