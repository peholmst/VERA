#include <stdexcept>
#include <SDL3/SDL.h>

class SDLException final : public std::runtime_error
{
public:
    explicit SDLException(const std::string &message) : std::runtime_error(message + '\n' + SDL_GetError())
    {
    }
};

int main()
{
    if (!SDL_Init(SDL_INIT_VIDEO))
    {
        throw SDLException("SDL_Init failed");
    }

    if (!SDL_HideCursor())
    {
        throw SDLException("SDL_HideCursor failed");
    }

    SDL_Window *window{SDL_CreateWindow("VERA Station Alert", 800, 600, SDL_WINDOW_HIDDEN | SDL_WINDOW_FULLSCREEN)}; // TODO Make full size

    if (!window)
    {
        throw SDLException("SDL_CreateWindow failed");
    }

    SDL_Renderer *renderer{SDL_CreateRenderer(window, nullptr)};
    if (!renderer)
    {
        throw SDLException("SDL_CreateRenderer failed");
    }
    else 
    {
        int w, h;
        SDL_GetCurrentRenderOutputSize(renderer, &w, &h);
        SDL_Log("Renderer: %s, Output size: %dx%d", SDL_GetRendererName(renderer), w, h);
    }

    SDL_SetRenderDrawColor(renderer, 0, 0, 255, 255);
    SDL_RenderClear(renderer);
    SDL_RenderPresent(renderer);

    SDL_ShowWindow(window);

    bool isRunning{true};
    SDL_Event event;

    // Main loop
    while (isRunning)
    {
        // Event loop
        while (SDL_PollEvent(&event))
        {
            if (event.type == SDL_EVENT_QUIT)
            {
                isRunning = false;
            }
        }
        SDL_Delay(16);
    }

    return EXIT_SUCCESS;
}