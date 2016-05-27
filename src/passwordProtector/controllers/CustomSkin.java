package passwordProtector.controllers;

import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;

class CustomSkin extends TextFieldSkin {

    private static final BooleanProperty MASK_PROPERTY = new SimpleBooleanProperty(false);
    public CustomSkin(final TextField textField) {
        super(textField);
    }

    public BooleanProperty getMaskProperty(){
        return MASK_PROPERTY;
    }

    @Override
    protected String maskText(String txt) {
        if (!MASK_PROPERTY.getValue()) {
            int n = txt.length();
            StringBuilder passwordBuilder = new StringBuilder(n);
            for (int i = 0; i < n; i++) {
                passwordBuilder.append(BULLET);
            }

            return passwordBuilder.toString();
        } else {
            return txt;
        }
    }
}
