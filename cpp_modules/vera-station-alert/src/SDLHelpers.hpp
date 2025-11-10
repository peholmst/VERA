#ifndef __SDL_HELPERS_HPP__
#define __SDL_HELPERS_HPP__

#include <SDL3/SDL.h>

#include <memory>
#include <stdexcept>

class SDLException final : public std::runtime_error
{
public:
    explicit SDLException(const std::string &message) : std::runtime_error(message + '\n' + SDL_GetError())
    {
    }
};

struct SDLDeleter
{
    void operator()(SDL_Window *p) const noexcept
    {
        if (p)
        {
            SDL_DestroyWindow(p);
        }
    }

    void operator()(SDL_Renderer *p) const noexcept
    {
        if (p)
        {
            SDL_DestroyRenderer(p);
        }
    }
};

using WindowPtr = std::unique_ptr<SDL_Window, SDLDeleter>;
using RendererPtr = std::unique_ptr<SDL_Renderer, SDLDeleter>;

inline void SDLCheck(bool success, const char *context)
{
    if (!success)
    {
        throw SDLException(context);
    }
}

struct SDLInitGuard
{
    SDLInitGuard(SDL_InitFlags flags)
    {
        SDLCheck(SDL_Init(flags), "SDL_Init");
    }
    ~SDLInitGuard()
    {
        SDL_Quit();
    }
};

struct Size
{
    int width = 0, height = 0;
};

inline Size GetWindowSize(const WindowPtr &window)
{
    int w, h;
    SDLCheck(SDL_GetWindowSize(window.get(), &w, &h), "SDL_GetWindowSize");
    return Size{w, h};
}

inline void SetRendererSize(const RendererPtr &renderer, const Size &size)
{
    SDLCheck(SDL_SetRenderLogicalPresentation(renderer.get(), size.width, size.height, SDL_LOGICAL_PRESENTATION_DISABLED), "SDL_SetRenderLogicalPresentation");
}

inline Size GetRendererSize(const RendererPtr &renderer)
{
    int w, h;
    SDLCheck(SDL_GetCurrentRenderOutputSize(renderer.get(), &w, &h), "SDL_GetCurrentRenderOutputSize");
    return Size{w, h};
}

inline uint32_t RGBA(Uint8 r, Uint8 g, Uint8 b, Uint8 a = 255)
{
#if SDL_BYTEORDER == SDL_BIG_ENDIAN
    return (r << 24) | (g << 16) | (b << 8) | a;
#else
    return (a << 24) | (b << 16) | (g << 8) | r;
#endif
}

#endif