package com.netcafe.ui.component;

import com.netcafe.ui.ThemeConfig;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * A styled text field with placeholder and focus effects.
 * 
 * Usage:
 * InputField field = new InputField("Enter amount...");
 * InputField field = new InputField("Search...", 20);
 */
public class InputField extends JTextField {

    private static final Color BORDER_NORMAL = new Color(209, 213, 219);
    private static final Color BORDER_FOCUS = ThemeConfig.PRIMARY;
    private static final Color PLACEHOLDER_COLOR = new Color(156, 163, 175);

    private String placeholder;
    private boolean showingPlaceholder = true;

    public InputField(String placeholder) {
        this(placeholder, 15);
    }

    public InputField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;

        setFont(new Font("SansSerif", Font.PLAIN, 13));
        setForeground(PLACEHOLDER_COLOR);
        setText(placeholder);

        setBorder(createBorder(BORDER_NORMAL));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    setText("");
                    setForeground(ThemeConfig.TEXT_PRIMARY);
                    showingPlaceholder = false;
                }
                setBorder(createBorder(BORDER_FOCUS));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(placeholder);
                    setForeground(PLACEHOLDER_COLOR);
                    showingPlaceholder = true;
                }
                setBorder(createBorder(BORDER_NORMAL));
            }
        });
    }

    private Border createBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    @Override
    public String getText() {
        if (showingPlaceholder) {
            return "";
        }
        return super.getText();
    }

    public void setError(boolean error) {
        if (error) {
            setBorder(createBorder(ThemeConfig.DANGER));
        } else {
            setBorder(createBorder(BORDER_NORMAL));
        }
    }
}
