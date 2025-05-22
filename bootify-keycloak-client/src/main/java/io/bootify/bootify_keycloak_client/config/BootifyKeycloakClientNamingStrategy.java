package io.bootify.bootify_keycloak_client.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;


public class BootifyKeycloakClientNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    private static final String TABLE_PREFIX = "Tb";

    private Identifier adjustName(final Identifier name, final boolean withAppendix) {
        if (name == null) {
            return null;
        }
        final String adjustedName = (withAppendix ? TABLE_PREFIX : "") + name.getText();
        return new Identifier(adjustedName, true);
    }

    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
        return adjustName(name, true);
    }

    @Override
    public Identifier toPhysicalColumnName(final Identifier name, final JdbcEnvironment context) {
        return adjustName(name, false);
    }

}
