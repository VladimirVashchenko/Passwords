package passwordProtector.controllers;

import javafx.scene.control.PasswordField;
import javafx.scene.control.Skin;

public class CustomTextField extends PasswordField {
    private CustomSkin customSkin;

    public CustomTextField() {

    }

    @Override
    protected Skin<?> createDefaultSkin() {
        customSkin = new CustomSkin(this);
        return customSkin;
    }
}

