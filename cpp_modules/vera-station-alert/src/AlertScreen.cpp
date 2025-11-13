#include "AlertScreen.hpp"

AlertScreen::AlertScreen(const RendererPtr &renderer) : renderer(renderer)
{
    size = GetRendererSize(renderer);
}

void AlertScreen::Paint(const Alert &alert)
{
    // TODO Implement me
}
