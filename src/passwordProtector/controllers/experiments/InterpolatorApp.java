/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package passwordProtector.controllers.experiments;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A sample that shows various types of interpolation between key frames in a
 * timeline. There are five circles, each animated with a different
 * interpolation method. The Linear interpolator is the default. Use the
 * controls to reduce opacity to zero for some circles to compare with others,
 * or change circle color to distinguish between individual interpolators.
 *
 * @sampleName Interpolator
 * @preview preview.png
 * @playground - (name="LINEAR")
 * @playground circle1.opacity (min=0, max=1)
 * @playground circle1.fill
 * @playground - (name="EASE_BOTH")
 * @playground circle2.opacity (min=0, max=1)
 * @playground circle2.fill
 * @playground - (name="EASE_IN")
 * @playground circle3.opacity (min=0, max=1)
 * @playground circle3.fill
 * @playground - (name="EASE_OUT")
 * @playground circle4.opacity (min=0, max=1)
 * @playground circle4.fill
 * @playground - (name="SPLINE")
 * @playground circle5.opacity (min=0, max=1)
 * @playground circle5.fill
 * @embedded
 * @see Interpolator
 * @see KeyFrame
 * @see KeyValue
 * @see Timeline
 * @see Duration
 */
public class InterpolatorApp extends Application {


    private final Timeline timeline = new Timeline();
    private Circle circle1;
    private Circle circle2;
    private Circle circle3;
    private Circle circle4;
    private Circle circle5;
    Rectangle rect = new Rectangle(-25, -25, 50, 50);
    public Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(245, 230);
        root.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        root.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        //create circles by method createMovingCircle listed below
        //default interpolator
        circle1 = createMovingCircle(Interpolator.LINEAR, 0, Color.RED);
        //circle slows down when reached both ends of trajectory
        circle2 = createMovingCircle(Interpolator.EASE_BOTH, 35, Color.VIOLET);
        //circle slows down in the beginning of animation
        circle3 = createMovingCircle(Interpolator.EASE_IN, 70, Color.BLUE);
        //circle slows down in the end of animation
        circle4 = createMovingCircle(Interpolator.EASE_OUT, 105, Color.YELLOW);
        //one can define own behaviour of interpolator by spline method
        circle5 = createMovingCircle(Interpolator.SPLINE(0.5, 0.1, 0.1, 0.5), 140, Color.GREEN);


        // set initial opacities
        circle1.setOpacity(0.7);
        circle2.setOpacity(0.45);
        circle3.setOpacity(0.20);
        circle4.setOpacity(0.35);
        circle5.setOpacity(0.7);

        root.getChildren().addAll(
                //circle1,
                /*circle2,
                circle3,
                circle4,
                circle5,*/
        rect);
        return root;
    }

    private Circle createMovingCircle(Interpolator interpolator, double centerY, Color color) {
        //create a transparent circle
        Circle circle = new Circle(45, 45, 40, color);
        circle.setOpacity(0);
        circle.setCenterY(centerY + 40);
        //add effect
        circle.setEffect(new Lighting());
        //create a timeline for moving the circle
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        //create a keyValue for horizontal translation of circle to the position 155px with given interpolator
        KeyValue keyValue = new KeyValue(circle.translateXProperty(), 155, interpolator);
        //create a keyFrame with duration 4s
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(4), keyValue);
        //add the keyframe to the timeline
        timeline.getKeyFrames().add(keyFrame);
        return circle;
    }

    public void play() {
        timeline.play();
    }

    @Override
    public void stop() {
        timeline.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        Scene scene = new Scene(createContent());
        primaryStage.setScene(scene);




        rect.setArcHeight(15);
        rect.setArcWidth(15);
        rect.setFill(Color.CRIMSON);
        rect.setTranslateX(50);
        rect.setTranslateY(75);

        Interpolator rectInterpolator = new Interpolator() {
            private static final double S1 = 25.0 / 9.0;
            private static final double S3 = 10.0 / 9.0;
            private static final double S4 = 1.0 / 9.0;

            @Override
            protected double curve(double t) {
                // See the SMIL 3.1 specification for details on this calculation
                // acceleration = 0.2, deceleration = 0.0
                return clamp((t < 0.2) ? S1 * t * t : S3 * t - S4);
            }

            private double clamp(double t) {
                return (t < 0.0) ? 0.0 : (t > 1.0) ? 1.0 : t;
            }
            /*@Override
            protected double curve(double t) {
                return  Math.exp(t);
            }*/
        };



        KeyValue rectKeyValue = new KeyValue(rect.rotateProperty(), -1800, rectInterpolator);
        //create a keyFrame with duration 4s
        KeyFrame rectKeyFrame = new KeyFrame(Duration.seconds(4), rectKeyValue);
        //add the keyframe to the timeline
        timeline.getKeyFrames().add(rectKeyFrame);

        KeyFrame moveBall = new KeyFrame(Duration.seconds(4),
                event -> {
                    //timeline.getKeyFrames().add(rectKeyFrame);
                    timeline.pause();
                    timeline.playFrom(Duration.seconds(3.297));
                });

timeline.getKeyFrames().add(moveBall);
        primaryStage.show();
        play();
    }

    /**
     * Java main for when running without JavaFX launcher
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
