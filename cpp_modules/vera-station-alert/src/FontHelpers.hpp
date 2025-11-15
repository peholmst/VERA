#ifndef __FONT_MANAGER_HPP__
#define __FONT_MANAGER_HPP__

#include "Fonts.hpp"
#include "SDLHelpers.hpp"

FontPtr CreateFont(const void *data, const size_t dataSize, const float &pointSize)
{
    FontPtr font{
        TTF_OpenFontIO(SDL_IOFromConstMem(data, dataSize), true, pointSize),
        SDLDeleter{}};
    SDLCheck(font != nullptr, "SDL_OpenFontIO");
    return font;
}

FontPtr CreateRobotoBlack(const float &pointSize)
{
    return CreateFont(Fonts::Roboto_Black_ttf, Fonts::Roboto_Black_ttf_size, pointSize);
}

FontPtr CreateRobotoBold(const float &pointSize)
{
    return CreateFont(Fonts::Roboto_Bold_ttf, Fonts::Roboto_Bold_ttf_size, pointSize);
}

FontPtr CreateRobotoExtraBold(const float &pointSize)
{
    return CreateFont(Fonts::Roboto_ExtraBold_ttf, Fonts::Roboto_ExtraBold_ttf_size, pointSize);
}

FontPtr CreateRobotoMedium(const float &pointSize)
{
    return CreateFont(Fonts::Roboto_Medium_ttf, Fonts::Roboto_Medium_ttf_size, pointSize);
}

FontPtr CreateRobotoRegular(const float &pointSize)
{
    return CreateFont(Fonts::Roboto_Regular_ttf, Fonts::Roboto_Regular_ttf_size, pointSize);
}

FontPtr CreateRobotoSemiBold(const float &pointSize)
{
    return CreateFont(Fonts::Roboto_SemiBold_ttf, Fonts::Roboto_SemiBold_ttf_size, pointSize);
}

#endif
