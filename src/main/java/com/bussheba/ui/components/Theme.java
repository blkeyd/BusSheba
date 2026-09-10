package com.bussheba.ui.components;

import java.awt.Color;

/**
 * Shared color palette for the "railway ticketing" style UI (light
 * background, teal primary, orange accent for calls to action) — used by
 * every screen EXCEPT LoginFrame/RegisterFrame, which intentionally keep
 * their own dark purple/indigo gradient theme.
 */
public final class Theme {

    public static final Color TEAL_PRIMARY = new Color(0, 121, 107);      // header bars, primary buttons
    public static final Color TEAL_DARK = new Color(0, 96, 85);           // hover state for teal buttons
    public static final Color TEAL_LIGHT = new Color(224, 242, 241);      // subtle tinted backgrounds/badges

    public static final Color ACCENT_ORANGE = new Color(230, 126, 34);    // primary call-to-action (Book/Confirm)
    public static final Color ACCENT_ORANGE_DARK = new Color(202, 106, 18);

    public static final Color BG_LIGHT = new Color(245, 247, 246);        // page background
    public static final Color CARD_WHITE = Color.WHITE;                  // cards/panels/table background
    public static final Color BORDER_LIGHT = new Color(224, 224, 224);

    public static final Color TEXT_DARK = new Color(33, 37, 41);
    public static final Color TEXT_MUTED = new Color(108, 117, 125);
    public static final Color TEXT_ON_TEAL = Color.WHITE;

    public static final Color DANGER_RED = new Color(211, 47, 47);
    public static final Color DANGER_RED_DARK = new Color(183, 28, 28);

    public static final Color SEAT_AVAILABLE = new Color(0, 150, 136);
    public static final Color SEAT_BOOKED = new Color(224, 224, 224);
    public static final Color SEAT_SELECTED = ACCENT_ORANGE;

    private Theme() {
        // Constants only.
    }
}
