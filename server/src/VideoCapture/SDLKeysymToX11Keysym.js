/**
 * convert SDL keysym to X11 keysym
 * part of source code from gaminganywhere and X11
 *
 */


var keymap = {};

/// macro

function SDL_SCANCODE_TO_KEYCODE(X) {
    return X | (1 << 30);
}


module.exports.SDLKeyToKeySym_init = () => {
    var i;
    //
    keymap['\b'.charCodeAt(0)] = 0xff08; //		= 8,
    keymap['\t'.charCodeAt(0)] = 0xff09; //		= 9,
    keymap[SDL_SCANCODE_TO_KEYCODE(156)] = 0xff0b; //		= 12,
    keymap['\r'.charCodeAt(0)] = 0xff0d; //		= 13,
    keymap[SDL_SCANCODE_TO_KEYCODE(72)] = 0xff13; //		= 19,
    keymap['\033'.charCodeAt(0)] = 0xff1b; //		= 27,
    // Latin 1: starting from space (0x20)
    // (0x20) space, exclam, quotedbl, numbersign, dollar, percent, ampersand,
    // (0x27) quoteright, parentleft, parentright, asterisk, plus, comma,
    // (0x2d) minus, period, slash
    // (0x30-0x39) 0-9
    // (0x3a) colon, semicolon, less, equal, greater, question, at
    // (0x41-0x5a) A-Z
    // (0x5b) bracketleft, backslash, bracketright, asciicircum/caret,
    // (0x5f) underscore, grave
    // (0x61-7a) a-z
    for (i = 0x20; i <= 0xFF; i++) {
        keymap[i] = i;
    }
    keymap['\177'.charCodeAt(0)] = 0xffff; //		= 127,

    /** @name Numeric keypad */
    keymap[SDL_SCANCODE_TO_KEYCODE(98)] = 0xffb0; //		= 256,
    keymap[SDL_SCANCODE_TO_KEYCODE(89)] = 0xffb1; //		= 257,
    keymap[SDL_SCANCODE_TO_KEYCODE(90)] = 0xffb2; //		= 258,
    keymap[SDL_SCANCODE_TO_KEYCODE(91)] = 0xffb3; //		= 259,
    keymap[SDL_SCANCODE_TO_KEYCODE(92)] = 0xffb4; //		= 260,
    keymap[SDL_SCANCODE_TO_KEYCODE(93)] = 0xffb5; //		= 261,
    keymap[SDL_SCANCODE_TO_KEYCODE(94)] = 0xffb6; //		= 262,
    keymap[SDL_SCANCODE_TO_KEYCODE(95)] = 0xffb7; //		= 263,
    keymap[SDL_SCANCODE_TO_KEYCODE(96)] = 0xffb8; //		= 264,
    keymap[SDL_SCANCODE_TO_KEYCODE(97)] = 0xffb9; //		= 265,
    keymap[SDL_SCANCODE_TO_KEYCODE(99)] = 0xff9f; //		= 266,
    keymap[SDL_SCANCODE_TO_KEYCODE(84)] = 0xffaf; //		= 267,
    keymap[SDL_SCANCODE_TO_KEYCODE(85)] = 0xffaa; //	    = 268,
    keymap[SDL_SCANCODE_TO_KEYCODE(86)] = 0xffad; //		= 269,
    keymap[SDL_SCANCODE_TO_KEYCODE(87)] = 0xffab; //		= 270,
    keymap[SDL_SCANCODE_TO_KEYCODE(88)] = 0xff8d; //		= 271,
    keymap[SDL_SCANCODE_TO_KEYCODE(103)] = 0xffbd; //		= 272,

    /** @name Arrows + Home/End pad */
    keymap[SDL_SCANCODE_TO_KEYCODE(82)] = 0xff52; //		= 273,
    keymap[SDL_SCANCODE_TO_KEYCODE(81)] = 0xff54; //		= 274,
    keymap[SDL_SCANCODE_TO_KEYCODE(79)] = 0xff53; //		= 275,
    keymap[SDL_SCANCODE_TO_KEYCODE(80)] = 0xff51; //		= 276,
    keymap[SDL_SCANCODE_TO_KEYCODE(73)] = 0xff63; //		= 277,
    keymap[SDL_SCANCODE_TO_KEYCODE(74)] = 0xff50; //		= 278,
    keymap[SDL_SCANCODE_TO_KEYCODE(77)] = 0xff57; //		= 279,
    keymap[SDL_SCANCODE_TO_KEYCODE(75)] = 0xff55; //		= 280,
    keymap[SDL_SCANCODE_TO_KEYCODE(78)] = 0xff56; //		= 281,

    /** @name Function keys */
    keymap[SDL_SCANCODE_TO_KEYCODE(58)] = 0xffbe; //		= 282,
    keymap[SDL_SCANCODE_TO_KEYCODE(59)] = 0xffbf; //		= 283,
    keymap[SDL_SCANCODE_TO_KEYCODE(60)] = 0xffc0; //		= 284,
    keymap[SDL_SCANCODE_TO_KEYCODE(61)] = 0xffc1; //		= 285,
    keymap[SDL_SCANCODE_TO_KEYCODE(62)] = 0xffc2; //		= 286,
    keymap[SDL_SCANCODE_TO_KEYCODE(63)] = 0xffc3; //		= 287,
    keymap[SDL_SCANCODE_TO_KEYCODE(64)] = 0xffc4; //		= 288,
    keymap[SDL_SCANCODE_TO_KEYCODE(65)] = 0xffc5; //		= 289,
    keymap[SDL_SCANCODE_TO_KEYCODE(66)] = 0xffc6; //		= 290,
    keymap[SDL_SCANCODE_TO_KEYCODE(67)] = 0xffc7; //		= 291,
    keymap[SDL_SCANCODE_TO_KEYCODE(68)] = 0xffc8; //		= 292,
    keymap[SDL_SCANCODE_TO_KEYCODE(69)] = 0xffc9; //		= 293,
    keymap[SDL_SCANCODE_TO_KEYCODE(104)] = 0xffca; //		= 294,
    keymap[SDL_SCANCODE_TO_KEYCODE(105)] = 0xffcb; //		= 295,
    keymap[SDL_SCANCODE_TO_KEYCODE(106)] = 0xffcc; //		= 296,

    /** @name Key state modifier keys */
    keymap[SDL_SCANCODE_TO_KEYCODE(83)] = 0xff7f; //		= 300,
    keymap[SDL_SCANCODE_TO_KEYCODE(57)] = 0xffe5; //		= 301,
    //keymap[SDLK_SCROLLOCK] = 0xff14; //		= 302,
    keymap[SDL_SCANCODE_TO_KEYCODE(229)] = 0xffe2; //		= 303,
    keymap[SDL_SCANCODE_TO_KEYCODE(225)] = 0xffe1; //		= 304,
    keymap[SDL_SCANCODE_TO_KEYCODE(228)] = 0xffe4; //		= 305,
    keymap[SDL_SCANCODE_TO_KEYCODE(224)] = 0xffe3; //		= 306,
    keymap[SDL_SCANCODE_TO_KEYCODE(230)] = 0xffea; //		= 307,
    keymap[SDL_SCANCODE_TO_KEYCODE(226)] = 0xffe9; //		= 308,
    //keymap[SDLK_RMETA] = 0xffe8; //		= 309,
    //keymap[SDLK_LMETA] = 0xffe7; //		= 310,

    /** @name Miscellaneous function keys */
    keymap[SDL_SCANCODE_TO_KEYCODE(117)] = 0xff6a; //		= 315,
    keymap[SDL_SCANCODE_TO_KEYCODE(154)] = 0xff15; //		= 317,
    //keymap[SDLK_BREAK] = 0xff6b; //		= 318,
    keymap[SDL_SCANCODE_TO_KEYCODE(118)] = 0xff67; //		= 319,
    keymap[1073741824] = 0xffe3;
}


module.exports.SDLKeyToKeySym = sdlkey => keymap[sdlkey]
