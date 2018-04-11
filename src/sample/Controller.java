package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    private LineChart<Number, Number> MyChart;
    @FXML
    private LineChart<Number, Number> ErrorTable;
    @FXML
    private LineChart<Number, Number> TruncError;
    @FXML
    private CheckBox eulerBox;
    @FXML
    private CheckBox improvedBox;
    @FXML
    private CheckBox rungeBox;
    @FXML
    private CheckBox exactBox;
    @FXML
    private TextField inputN;
    @FXML
    private Label labelN;

    private Series euler_ser = new Series();
    private Series improved_ser = new Series();
    private Series rungeKutta_ser = new Series();
    private Series exact_ser = new Series();
    private Series eulerError_ser = new Series();
    private Series improvedError_ser = new Series();
    private Series rungeKuttaError_ser = new Series();

    private int N = 100;
    private double x0 = 0, X = 8;
//    private double h = (X - x0) / N;
    private double x[] = new double[N];

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        calculate();
        int minN = 50;
        int maxN = 100;
        double eulerTruncError[] = new double[maxN - minN];
        for (int i = minN; i < maxN; i++) {
            eulerTruncError[i-minN] = eulerMaxError(i);
        }
        Series eulerTruncError_ser = new Series();
        eulerTruncError_ser.setName("Euler truncation error");
        for (int i = 1; i < maxN - minN; i++) {
            eulerTruncError_ser.getData().add(new Data(i + minN, eulerTruncError[i]));
        }

        TruncError.getData().add(eulerTruncError_ser);
    }

    private double func(double x, double y) {
        return (y * Math.cos(x) - y * Math.tan(x));
    }

    private void calculate() {
        euler_ser = eulerApproximation(N);
        improved_ser = improvedApproximation(N);
        rungeKutta_ser = rungeApproximation(N);
        exact_ser = exactSolution(N);

    }

    private Series eulerApproximation(int n) {
        double euler[] = eulerArray(n);
        double eulerError[] = eulerError(n);
        for (int i = 1; i < n; i++) {
            euler_ser.getData().add(new Data(x[i], euler[i]));
            eulerError_ser.getData().add(new Data(x[i], eulerError[i]));
        }
        euler_ser.setName("Euler");
        eulerError_ser.setName("EulerError");
        return euler_ser;
    }

    private double[] eulerError(int n) {
        double euler[] = eulerArray(n);
        double eulerError[] = new double[n];
        double exact[] = exactArray(n);
        for (int i = 1; i < n; i++) {
            eulerError[i] = Math.abs(exact[i] - euler[i]);
        }
        return eulerError;
    }

    private double[] eulerArray(int n) {
        double euler[] = new double[n];
        x[0] = x0;
        double h = (X - x0) / n;
        euler[0] = 1;
        for (int i = 1; i < n; i++) {
            x[i] = x[i - 1] + h;
            euler[i] = euler[i - 1] + h * func(x[i - 1], euler[i - 1]);
        }
        return euler;
    }

    private double eulerMaxError(int n) {
        double eulerError[] = eulerError(n);
        double max_error = 0;
        for (int i = 0; i < n; i++) {
            if (max_error < eulerError[i])
                max_error = eulerError[i];
        }
        return max_error;
    }

    private Series improvedApproximation(int n) {
        double improved[] = new double[n];
        double improvedError[] = new double[n];
        double exact[] = exactArray(n);
        x[0] = x0;
        double h = (X - x0) / n;
        improved[0] = 1;
        for (int i = 1; i < n; i++) {
            x[i] = x[i - 1] + h;
            double k1 = h * (func(x[i - 1], improved[i - 1]));
            double k2 = h * (func(x[i - 1] + h, improved[i - 1] + k1));
            improved[i] = improved[i - 1] + (1.0 / 2.0) * (k1 + k2);
            improvedError[i] = Math.abs(exact[i] - improved[i]);
            improved_ser.getData().add(new Data(x[i], improved[i]));
            improvedError_ser.getData().add(new Data(x[i], improvedError[i]));
        }
        improved_ser.setName("Improved");
        improvedError_ser.setName("ImprovedError");
        return improved_ser;
    }

    private Series rungeApproximation(int n) {
        double rungeKutta[] = new double[n];
        double rungeKuttaError[] = new double[n];
        double exact[] = exactArray(n);
        x[0] = x0;
        double h = (X - x0) / n;
        rungeKutta[0] = 1;
        for (int i = 1; i < n; i++) {
            x[i] = x[i - 1] + h;
            double kr1 = h * (func(x[i - 1], rungeKutta[i - 1]));
            double kr2 = h * (func(x[i - 1] + 0.5 * h, rungeKutta[i - 1] + 0.5 * kr1));
            double kr3 = h * (func(x[i - 1] + 0.5 * h, rungeKutta[i - 1] + 0.5 * kr2));
            double kr4 = h * (func(x[i - 1] + h, rungeKutta[i - 1] + kr3));
            rungeKutta[i] = rungeKutta[i - 1] + (1.0 / 6.0) * (kr1 + 2 * kr2 + 2 * kr3 + kr4);
            rungeKuttaError[i] = Math.abs(exact[i] - rungeKutta[i]);
            rungeKutta_ser.getData().add(new Data(x[i], rungeKutta[i]));
            rungeKuttaError_ser.getData().add(new Data(x[i], rungeKuttaError[i]));
        }
        rungeKutta_ser.setName("RungeKutta");
        rungeKuttaError_ser.setName("RungeKuttaError");
        return rungeKutta_ser;
    }

    private Series exactSolution(int n) {
        double exact[] = exactArray(n);
        for (int i = 1; i < n; i++) {
            exact_ser.getData().add(new Data(x[i], exact[i]));
        }
        exact_ser.setName("Exact");
        return exact_ser;
    }

    private double[] exactArray(int n) {
        double exact[] = new double[n];
        x[0] = x0;
        double h = (X - x0) / n;
        exact[0] = 1;
        for (int i = 1; i < n; i++) {
            x[i] = x[i - 1] + h;
            exact[i] = Math.cos(x[i]) * Math.pow(Math.E, Math.sin(x[i]));
        }
        return exact;
    }

    private void resetAll() {
        MyChart.getData().removeAll(euler_ser, improved_ser, rungeKutta_ser, exact_ser);
        ErrorTable.getData().removeAll(eulerError_ser, improvedError_ser, rungeKuttaError_ser);
        eulerBox.setSelected(false);
        improvedBox.setSelected(false);
        rungeBox.setSelected(false);
        exactBox.setSelected(false);
    }

    private void reset(Series solution, Series error) {
        MyChart.getData().remove(solution);
        ErrorTable.getData().remove(error);
    }

    @FXML
    private void eulerChecked(ActionEvent e) {
        if (eulerBox.isSelected()) {
            MyChart.getData().add(euler_ser);
            ErrorTable.getData().add(eulerError_ser);
        } else {
            reset(euler_ser, eulerError_ser);
        }
    }

    @FXML
    private void improvedChecked(ActionEvent e) {
        if (improvedBox.isSelected()) {
            MyChart.getData().add(improved_ser);
            ErrorTable.getData().add(improvedError_ser);
        } else {
            reset(improved_ser, improvedError_ser);
        }
    }

    @FXML
    private void rungeChecked(ActionEvent e) {
        if (rungeBox.isSelected()) {
            MyChart.getData().add(rungeKutta_ser);
            ErrorTable.getData().add(rungeKuttaError_ser);
        } else {
            reset(rungeKutta_ser, rungeKuttaError_ser);
        }
    }

    @FXML
    private void exactChecked(ActionEvent e) {
        if (exactBox.isSelected()) {
            MyChart.getData().add(exact_ser);
        } else {
            MyChart.getData().remove(exact_ser);
        }
    }

    @FXML
    private void getN(ActionEvent e) {
        resetAll();
        N = Integer.parseInt(inputN.getText());
        labelN.setText(String.valueOf(N));
        calculate();
    }
}
