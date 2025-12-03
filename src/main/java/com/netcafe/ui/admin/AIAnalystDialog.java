package com.netcafe.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AIAnalystDialog extends JDialog {

    public AIAnalystDialog(Frame owner) {
        super(owner, "NetCafe Jarvis - AI Business Analyst", false); // Non-modal
        setLayout(new BorderLayout());

        add(new AIAnalystPanel(), BorderLayout.CENTER);

        setSize(800, 600);
        setLocationRelativeTo(owner);
    }
}
