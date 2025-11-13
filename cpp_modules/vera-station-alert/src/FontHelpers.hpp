#ifndef __FONT_MANAGER_HPP__
#define __FONT_MANAGER_HPP__

#include "Fonts.hpp"
#include "SDLHelpers.hpp"

FontPtr CreateRobotoRegular(const float &pointSize)
{
    FontPtr font{
        TTF_OpenFontIO(SDL_IOFromConstMem(Fonts::Roboto_Regular_ttf, Fonts::Roboto_Regular_ttf_size), true, pointSize),
        SDLDeleter{}  
    };
    SDLCheck(font != nullptr, "SDL_OpenFontIO");
    return font;
}

#endif
