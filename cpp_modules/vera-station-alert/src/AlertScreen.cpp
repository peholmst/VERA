#include "AlertScreen.hpp"
#include "FontHelpers.hpp"

const uint32_t ALERT_CODE_TEXT_COLOR = RGBA(24, 25, 27);

AlertScreen::AlertScreen(const RendererPtr &renderer) : renderer(renderer)
{
    textEngine = TextEnginePtr{TTF_CreateRendererTextEngine(renderer.get()), SDLDeleter{}};
    SDLCheck(textEngine != nullptr, "TTF_CreateRendererTextEngine");
    size = GetRendererSize(renderer);
}

void AlertScreen::Paint(const Alert &alert)
{
    auto font = CreateRobotoRegular(48.0f);
    SDL_RenderClear(renderer.get());
    PaintText(textEngine, font, "Hello World", 0, 0, ALERT_CODE_TEXT_COLOR);
    // TODO Make helpers for drawing text!
    SDL_RenderPresent(renderer.get());
}
