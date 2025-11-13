#include <SDL3/SDL.h>

#include "Alert.hpp"
#include "AlertScreen.hpp"
#include "AnalogClock.hpp"
#include "SDLHelpers.hpp"

// TODO Read from config file
const auto alertTimeout = std::chrono::seconds(180);
const auto flashDuration = std::chrono::seconds(10);

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
    AlertScreen alertScreen{renderer};
    ActiveAlerts activeAlerts{alertTimeout, flashDuration};

    // TODO Replace testAlert with Websocket client
    const Alert testAlert{
        Timestamp::clock::now(),
        "401",
        "B",
        "PARGAS",
        "Badhusgatan 4",
        "Lekstuga brinner",
        {"RVS911", "RVS903"}};
    activeAlerts.Push(testAlert); // TODO when this happens, also turn on the lights and sound

    // Main loop
    while (isRunning)
    {
        // Paint UI
        auto activeAlert = activeAlerts.Poll();
        if (activeAlert.has_value())
        {
            alertScreen.Paint(activeAlert.value());
        }
        else
        {
            clock.Paint();
        }

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