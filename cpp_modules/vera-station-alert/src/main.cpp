#include <SDL3/SDL.h>

#include "AnalogClock.hpp"
#include "SDLHelpers.hpp"

int main()
{
    SDLInitGuard sdl(SDL_INIT_VIDEO);
    SDLCheck(SDL_HideCursor(), "SDL_HideCursor");

    WindowPtr window{
        SDL_CreateWindow("VERA Station Alert", 800, 600, SDL_WINDOW_FULLSCREEN),
        SDLDeleter{}};
    SDLCheck(window != nullptr, "SDL_CreateWindow");

    RendererPtr renderer{
        SDL_CreateRenderer(window.get(), nullptr),
        SDLDeleter{}};
    SDLCheck(renderer != nullptr, "SDL_CreateRenderer");

    auto windowSize = GetWindowSize(window);
    SetRendererSize(renderer, windowSize);

    bool isRunning{true};
    SDL_Event event;
    AnalogClock clock{renderer};

    // Main loop
    while (isRunning)
    {
        clock.Paint();
        // Event loop
        while (SDL_PollEvent(&event))
        {
            if (event.type == SDL_EVENT_QUIT || event.type == SDL_EVENT_KEY_DOWN)
            {
                isRunning = false;
            }
        }
        SDL_Delay(16);
    }

    return EXIT_SUCCESS;
}