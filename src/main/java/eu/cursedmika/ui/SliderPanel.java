package eu.cursedmika.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.NumberFormat;

public class SliderPanel extends JPanel
{
    private final JSpinner spinner;
    private final SpinnerNumberModel model;

    public SliderPanel()
    {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        model = new SpinnerNumberModel(1d, 0.1d, null, 0.1d);
        spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#,##0.0");
        JFormattedTextField textField = editor.getTextField();
        textField.setColumns(12);
        spinner.setEditor(editor);

        add(new JLabel("Scale: "));
        add(spinner);

    }

    public void addChangeListener(ChangeListener listener)
    {
        spinner.addChangeListener(listener);
    }

    public double getSliderValue()
    {
        return model.getNumber().doubleValue();
    }

    public void setSliderValue(double value)
    {
        model.setValue(value);
    }

    public JSpinner getSlider()
    {
        return spinner;
    }
}