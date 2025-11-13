#include "AlertScreen.hpp"
#include "FontHelpers.hpp"

AlertScreen::AlertScreen(const RendererPtr &renderer) : renderer(renderer)
{
    size = GetRendererSize(renderer);
}

void AlertScreen::Paint(const Alert &alert)
{
    auto font = CreateRobotoRegular(18.0f);
    // TODO Make helpers for drawing text!
}
