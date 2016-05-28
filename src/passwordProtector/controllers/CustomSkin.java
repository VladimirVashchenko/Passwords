package passwordProtector.controllers;

import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.shape.SVGPath;

class CustomSkin extends TextFieldSkin {

    protected TextField textField;
    final Group openEye;
    final Group closedEye;
    private static final ToggleButton toggleButton = new ToggleButton();

    public CustomSkin(final TextField textField) {
        super(textField);
        this.textField = textField;

        SVGPath path1 = new SVGPath();
        SVGPath path2 = new SVGPath();

        path1.setContent("M0.0185825,11.2474508c0.3260324-0.5753508,0.6594008-1.1466064,0.9780971-1.7260532\n" +
                "\tc0.316443-0.5753508,0.6328861-1.1507025,0.9780968-1.7260542C1.571858,7.3303804,1.1689398,6.8654156,0.7660216,6.4004526\n" +
                "\tC0.0791067,5.8780479,0.0698413,4.952961,0.4852571,4.5538864c0.401181-0.3854008,1.2801549-0.3587208,1.7925732,0.291563\n" +
                "\tC2.6370933,5.2150393,2.996356,5.58463,3.355619,5.9542198c0.4602811-0.345211,1.0356321-0.6904202,1.495913-1.1507015\n" +
                "\tC4.627151,4.3783512,4.4027705,3.9531822,4.1783895,3.528017c-0.556329-0.7322292-0.3075469-1.6580925,0.1943755-1.9221592\n" +
                "\tc0.4545918-0.2391663,1.2446971-0.0027351,1.6197948,0.7127113C6.302639,2.8783875,6.6127172,3.4382057,6.9227962,3.9980259\n" +
                "\tc0.5753512-0.2301407,1.1507015-0.4602814,1.6109829-0.5753517c-0.1415615-0.6631985-0.2831249-1.326395-0.4246864-1.9895935\n" +
                "\tc-0.1592302-0.655344,0.302145-1.2778206,0.8854866-1.3390293c0.5532293-0.0580502,1.111105,0.3997383,1.1446562,1.0366688\n" +
                "\tc0.0785551,0.6489143,0.1571112,1.2978287,0.2356663,1.946743c0.4602804,0,0.8054914,0,1.2657738,0\n" +
                "\tc0.1114416-0.6345158,0.2228851-1.2690334,0.3343267-1.9035492c0.0504103-0.6627693,0.6724091-1.0925751,1.2094469-0.9718761\n" +
                "\tc0.5249577,0.117979,0.9015598,0.7468777,0.6911135,1.3606262c-0.1312542,0.6583614-0.2625093,1.316721-0.3937635,1.9750805\n" +
                "\tc0.1623831,0.0777283,0.3350382,0.1550064,0.5178156,0.2301407c0.2192802,0.0901413,0.430625,0.167942,0.6318512,0.2352695\n" +
                "\tc0.3311586-0.5687275,0.6623154-1.137455,0.993474-1.7061825c0.4203997-0.7781124,1.3128977-0.9367504,1.7277822-0.6263218\n" +
                "\tc0.3927441,0.2938614,0.5056915,1.1189384-0.0215969,1.7493782c-0.2858334,0.5378761-0.5716667,1.0757523-0.8575001,1.6136293\n" +
                "\tc0.2231064,0.2058983,0.4719772,0.4190559,0.7479572,0.6328859c0.215189,0.166728,0.4254799,0.3172989,0.6278763,0.4531431\n" +
                "\tc0.5183353-0.4679403,1.0366688-0.9358807,1.5550041-1.403821C19.8328056,4.275425,20.5476189,4.2967434,20.9375,4.7337503\n" +
                "\tc0.3947563,0.4424725,0.3271313,1.1661987-0.172411,1.5371189c-0.4859371,0.4751396-0.9718761,0.9502802-1.4578152,1.4254189\n" +
                "\tc0.3165016,0.5540724,0.6331234,1.1192923,0.9492874,1.6956549c0.3215084,0.5861063,0.6330509,1.1664238,0.9349442,1.7404375\n" +
                "\tc-0.3404751,0.6707087-0.8704052,1.5956669-1.6574593,2.5895452c-1.1826153,1.4933844-4.2630663,5.3833218-8.8139315,5.4653702\n" +
                "\tc-4.9242125,0.0887794-8.2987747-4.3397264-9.2305851-5.5625582C0.7967265,12.7155581,0.3263501,11.871974,0.0185825,11.2474508\n" +
                "\t M3.7572429,11.0330658c0.9305646,3.0472746,3.7455642,5.1420555,6.9003267,5.1509476\n" +
                "\tc3.2422085,0.00914,6.1278038-2.186862,6.9975128-5.3453226c-1.1413307-3.06388-4.1311226-5.0344601-7.3214722-4.8593845\n" +
                "\tC7.3534536,6.1428461,4.7349772,8.1491756,3.7572429,11.0330658z M10.8260288,7.3531356\n" +
                "\tc1.9700975,0,3.5671778,1.5970798,3.5671778,3.5671773s-1.5970802,3.5671778-3.5671778,3.5671778\n" +
                "\ts-3.5671782-1.5970802-3.5671782-3.5671778S8.8559303,7.3531356,10.8260288,7.3531356z");

        path2.setContent("M20.2565918,11.8996267c-0.3117981,0.6290808-0.634613,1.1943445-0.9493408,1.6956787\n" +
                "\tc0.4859619,0.4751587,0.9719238,0.9502563,1.4578247,1.425415c0.4995728,0.3709106,0.5671997,1.0946665,0.1724243,1.5371103\n" +
                "\tc-0.3898926,0.4370117-1.1046753,0.458313-1.53302,0.0178833c-0.5183716-0.4679565-1.0366821-0.935853-1.5549927-1.4038095\n" +
                "\tc-0.2024536,0.1358643-0.4127197,0.286377-0.6279297,0.453125c-0.2759399,0.2138672-0.5248413,0.4270029-0.7479248,0.6328745\n" +
                "\tc0.2858276,0.5379028,0.5716553,1.0758057,0.8574829,1.6136475c0.5272827,0.6304321,0.4143677,1.4555054,0.0216064,1.7493896\n" +
                "\tc-0.414856,0.3104248-1.307373,0.1517944-1.7277832-0.6263428c-0.3311768-0.5687256-0.6622925-1.1374512-0.9934692-1.7061768\n" +
                "\tc-0.2012329,0.0673218-0.4125977,0.1451416-0.6318359,0.2352905c-0.1828003,0.0751343-0.3554688,0.1524048-0.5178223,0.2301025\n" +
                "\tc0.1312256,0.6583862,0.2625122,1.3167725,0.3937378,1.9750977c0.2104492,0.6137695-0.1661377,1.2426758-0.6911011,1.3606567\n" +
                "\tc-0.5370483,0.1206665-1.1590576-0.3091431-1.2094727-0.9719238c-0.1113892-0.6344604-0.2228394-1.2689819-0.3342896-1.9035034\n" +
                "\th-1.2658081c-0.0785522,0.6488647-0.1571045,1.2977905-0.2356567,1.9467163\n" +
                "\tc-0.0335083,0.6369629-0.5914307,1.0947266-1.1446533,1.0366821c-0.583313-0.0612183-1.0446777-0.6837158-0.885498-1.3390503\n" +
                "\tc0.1416016-0.663208,0.2831421-1.326355,0.4246826-1.989563c-0.4602661-0.1151123-1.0355835-0.3452148-1.6109619-0.5753784\n" +
                "\tc-0.3100586,0.5598145-0.6201782,1.1196289-0.9302368,1.6794434c-0.375061,0.7154541-1.1652222,0.9519043-1.619812,0.7127075\n" +
                "\tc-0.5018921-0.2640381-0.7506714-1.1898804-0.1943359-1.9221191c0.2243652-0.4251709,0.4487305-0.8503418,0.6731567-1.2755127\n" +
                "\tc-0.4603271-0.4602661-1.0356445-0.8054819-1.4959717-1.1506968c-0.3592529,0.3695679-0.7185059,0.7391977-1.0777588,1.1087656\n" +
                "\tc-0.5123901,0.6502686-1.3914185,0.6769409-1.7926025,0.2915649c-0.4154053-0.3990479-0.4061279-1.3241587,0.2807617-1.8465586\n" +
                "\tc0.4029541-0.4649658,0.8058472-0.9299316,1.2088013-1.3948975c-0.3452148-0.5753784-0.6616821-1.1506958-0.9780884-1.7260742\n" +
                "\tc-0.3187256-0.5794067-0.6520996-1.1506958-0.9781494-1.7260132c1.2462362,0.0714521,2.4924724,0.1429033,3.7387085,0.2143555\n" +
                "\tc0.9777222,2.8839111,3.5961914,4.8901978,6.576355,5.053772c3.1903687,0.1750488,6.1801147-1.7955322,7.3214722-4.859375\n" +
                "\tc1.144043-0.1622515,2.2880859-0.3245039,3.4321289-0.4867554C20.8683434,10.5632,20.5963497,11.2141333,20.2565918,11.8996267z\n" +
                "\t M21.0625,5.1400003c-2.0133114-0.9117098-4.4403667-1.8170671-7.25-2.500001c-5.352273-1.3009701-10.0475388-1.2976227-13.4375-1\n" +
                "\tc-0.1082887,0.2928925-0.2198281,0.7409401-0.0625,1.1875c0.714594,2.0283031,5.7323203,0.4957943,12.5625,2.187501\n" +
                "\tc4.1240273,1.0214443,7.0493641,2.7577839,8,1.625C21.2238922,6.2242584,21.1625938,5.593895,21.0625,5.1400003z");

        toggleButton.setCursor(Cursor.DEFAULT);
        toggleButton.setManaged(false);
        toggleButton.setVisible(false);

        toggleButton.getStyleClass().setAll("toggle-eye");

        openEye = new Group();
        closedEye = new Group();

        openEye.getChildren().addAll(path1);
        closedEye.getChildren().addAll(path2);

        toggleButton.setGraphic(closedEye);
        toggleButton.setFocusTraversable(false);

        toggleButton.selectedProperty().addListener((observable1, oldValue, newValue) -> {
            textField.setText(textField.getText());
            textField.end();
            if (newValue) {
                toggleButton.setGraphic(openEye);
            } else {
                toggleButton.setGraphic(closedEye);
            }
        });

        textField.textProperty().addListener(observable -> {
            toggleButton.visibleProperty().set(!textField.textProperty().get().isEmpty());
        });
    }

    @Override
    protected String maskText(String txt) {
        if (getSkinnable() instanceof PasswordField && !toggleButton.selectedProperty().get()) {
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

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        if (toggleButton.getParent() != textField) {
            getChildren().add(toggleButton);
            toggleButton.resize(snapSize(openEye.prefWidth(-1)), h);
            textField.setStyle("-fx-padding: 0.25em " + (h + 10) + "  0.333333em 0.416667em;");
        }

        positionInArea(toggleButton, textField.getWidth() - toggleButton.getWidth() - 3, y, snapSize(openEye.prefWidth(-1)), h, 0, HPos.CENTER, VPos.CENTER);
    }

}
