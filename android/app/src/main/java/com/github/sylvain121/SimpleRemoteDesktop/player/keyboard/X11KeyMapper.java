package com.github.sylvain121.SimpleRemoteDesktop.player.keyboard;

import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by sylvain on 15/02/18.
 */

public class X11KeyMapper {
    private static final String TAG = "X11_KEYCODE_MAPPER";

    public static int getKeysym(int keycode) {

    switch(keycode) {
        case  KeyEvent.KEYCODE_0:
            return X11KeyBoardDefinition.XK_0;
        case  KeyEvent.KEYCODE_1:
            return X11KeyBoardDefinition.XK_1;
        case  KeyEvent.KEYCODE_2:
            return X11KeyBoardDefinition.XK_2;
        case  KeyEvent.KEYCODE_3:
            return X11KeyBoardDefinition.XK_3;
        case  KeyEvent.KEYCODE_4:
            return X11KeyBoardDefinition.XK_4;
        case  KeyEvent.KEYCODE_5:
            return X11KeyBoardDefinition.XK_5;
        case  KeyEvent.KEYCODE_6:
            return X11KeyBoardDefinition.XK_6;
        case  KeyEvent.KEYCODE_7:
            return X11KeyBoardDefinition.XK_7;
        case  KeyEvent.KEYCODE_8:
            return X11KeyBoardDefinition.XK_8;
        case  KeyEvent.KEYCODE_9:
            return X11KeyBoardDefinition.XK_9;
        /*case  KEYCODE_STAR            = 17:
        case  KEYCODE_POUND           = 18:
        case  KEYCODE_DPAD_UP         = 19:
        case  KEYCODE_DPAD_DOWN       = 20:
        case  KEYCODE_DPAD_LEFT       = 21:
        case  KEYCODE_DPAD_RIGHT      = 22:
        case  KEYCODE_DPAD_CENTER     = 23:
        case  KEYCODE_CLEAR           = 28:
        case  KEYCODE_A               = 29:
        case  KEYCODE_B               = 30:
        case  KEYCODE_C               = 31:
        case  KEYCODE_D               = 32:
        case  KEYCODE_E               = 33:
        case  KEYCODE_F               = 34:
        case  KEYCODE_G               = 35:
        case  KEYCODE_H               = 36:
        case  KEYCODE_I               = 37:
        case  KEYCODE_J               = 38:
        case  KEYCODE_K               = 39:
        case  KEYCODE_L               = 40:
        case  KEYCODE_M               = 41:
        case  KEYCODE_N               = 42:
        case  KEYCODE_O               = 43:
        case  KEYCODE_P               = 44:
        case  KEYCODE_Q               = 45:
        case  KEYCODE_R               = 46:
        case  KEYCODE_S               = 47:
        case  KEYCODE_T               = 48:
        case  KEYCODE_U               = 49:
        case  KEYCODE_V               = 50:
        case  KEYCODE_W               = 51:
        case  KEYCODE_X               = 52:
        case  KEYCODE_Y               = 53:
        case  KEYCODE_Z               = 54:
        case  KEYCODE_COMMA           = 55:
        case  KEYCODE_PERIOD          = 56:
        case  KEYCODE_ALT_LEFT        = 57:
        case  KEYCODE_ALT_RIGHT       = 58:
        case  KEYCODE_SHIFT_LEFT      = 59:
        case  KEYCODE_SHIFT_RIGHT     = 60:
        case  KEYCODE_TAB             = 61:
        case  KEYCODE_SPACE           = 62:
        case  KEYCODE_SYM             = 63:
        case  KEYCODE_ENTER           = 66:
        case  KEYCODE_DEL             = 67:
        case  KEYCODE_GRAVE           = 68:
        case  KEYCODE_MINUS           = 69:
        case  KEYCODE_EQUALS          = 70:
        case  KEYCODE_LEFT_BRACKET    = 71:
        case  KEYCODE_RIGHT_BRACKET   = 72:
        case  KEYCODE_BACKSLASH       = 73:
        case  KEYCODE_SEMICOLON       = 74:
        case  KEYCODE_APOSTROPHE      = 75:
        case  KEYCODE_SLASH           = 76:
        case  KEYCODE_AT              = 77:
        case  KEYCODE_NUM             = 78:
        case  KEYCODE_HEADSETHOOK     = 79:
        case  KEYCODE_FOCUS           = 80:   // *Camera* focus
        case  KEYCODE_PLUS            = 81:
        case  KEYCODE_MENU            = 82:
        case  KEYCODE_NOTIFICATION    = 83:
        case  KEYCODE_SEARCH          = 84:
        case  KEYCODE_MEDIA_PLAY_PAUSE= 85:
        case  KEYCODE_MEDIA_STOP      = 86:
        case  KEYCODE_MEDIA_NEXT      = 87:
        case  KEYCODE_MEDIA_PREVIOUS  = 88:
        case  KEYCODE_MEDIA_REWIND    = 89:
        case  KEYCODE_MEDIA_FAST_FORWARD = 90:
        case  KEYCODE_MUTE            = 91:
        case  KEYCODE_PAGE_UP         = 92:
        case  KEYCODE_PAGE_DOWN       = 93:
        case  KEYCODE_PICTSYMBOLS     = 94:   // switch symbol-sets (Emoji,Kao-moji)
        case  KEYCODE_SWITCH_CHARSET  = 95:   // switch char-sets (Kanji,Katakana)
        case  KEYCODE_ESCAPE          = 111:
        case  KEYCODE_FORWARD_DEL     = 112:
        case  KEYCODE_CTRL_LEFT       = 113:
        case  KEYCODE_CTRL_RIGHT      = 114:
        case  KEYCODE_CAPS_LOCK       = 115:
        case  KEYCODE_SCROLL_LOCK     = 116:
        case  KEYCODE_META_LEFT       = 117:
        case  KEYCODE_META_RIGHT      = 118:
        case  KEYCODE_BREAK           = 121:
        case  KEYCODE_MOVE_HOME       = 122:
        case  KEYCODE_MOVE_END        = 123:
        case  KEYCODE_INSERT          = 124:
        case  KEYCODE_FORWARD         = 125:
        case  KEYCODE_F1              = 131:
        case  KEYCODE_F2              = 132:
        case  KEYCODE_F3              = 133:
        case  KEYCODE_F4              = 134:
        case  KEYCODE_F5              = 135:
        case  KEYCODE_F6              = 136:
        case  KEYCODE_F7              = 137:
        case  KEYCODE_F8              = 138:
        case  KEYCODE_F9              = 139:
        case  KEYCODE_F10             = 140:
        case  KEYCODE_F11             = 141:
        case  KEYCODE_F12             = 142:
        case  KEYCODE_NUM_LOCK        = 143:
        case  KEYCODE_NUMPAD_0        = 144:
        case  KEYCODE_NUMPAD_1        = 145:
        case  KEYCODE_NUMPAD_2        = 146:
        case  KEYCODE_NUMPAD_3        = 147:
        case  KEYCODE_NUMPAD_4        = 148:
        case  KEYCODE_NUMPAD_5        = 149:
        case  KEYCODE_NUMPAD_6        = 150:
        case  KEYCODE_NUMPAD_7        = 151:
        case  KEYCODE_NUMPAD_8        = 152:
        case  KEYCODE_NUMPAD_9        = 153:
        case  KEYCODE_NUMPAD_DIVIDE   = 154:
        case  KEYCODE_NUMPAD_MULTIPLY = 155:
        case  KEYCODE_NUMPAD_SUBTRACT = 156:
        case  KEYCODE_NUMPAD_ADD      = 157:
        case  KEYCODE_NUMPAD_DOT      = 158:
        case  KEYCODE_NUMPAD_COMMA    = 159:
        case  KEYCODE_NUMPAD_ENTER    = 160:
        case  KEYCODE_NUMPAD_EQUALS   = 161:
        case  KEYCODE_NUMPAD_LEFT_PAREN = 162:
        case  KEYCODE_NUMPAD_RIGHT_PAREN = 163:*/
        default:
            Log.d(TAG, "keycode : "+keycode+" not implemented");
            break;
        }
        return 0;
    }
}
