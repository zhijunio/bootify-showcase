package io.bootify.bootify_form_jwt_thymeleaf.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;


public class BootifyFormJwtThymeleafNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    private static final String TABLE_PREFIX = "TB_";

    private Identifier adjustName(final Identifier name, final boolean withAppendix) {
        if (name == null) {
            return null;
        }
        final String adjustedName = (withAppendix ? TABLE_PREFIX : "") + name.getText().toUpperCase();
        return new Identifier(adjustedName, true);
    }

    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
        return adjustName(super.toPhysicalTableName(name, context), true);
    }

    @Override
    public Identifier toPhysicalColumnName(final Identifier name, final JdbcEnvironment context) {
        return adjustName(super.toPhysicalColumnName(name, context), false);
    }

}
