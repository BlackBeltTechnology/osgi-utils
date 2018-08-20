package hu.blackbelt.osgi.utils.lang;

import hu.blackbelt.judo.framework.lang.entity.User;
import hu.blackbelt.judo.framework.lang.entity.UserBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapBuilderProxyTest {

    @Test
    public void testBuild() {
        User user = MapBuilderProxy.newInstance(UserBuilder.class, User.class).id("1").active(true).loginName("teszt").build();

        assertEquals("teszt", user.getLoginName());
        assertEquals("1", user.getId());
        assertEquals("teszt", ((MapHolder) user).toMap().get("loginName"));

    }
}
