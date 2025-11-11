#ifndef __ANALOG_CLOCK_HPP__
#define __ANALOG_CLOCK_HPP__

#include <SDL3/SDL.h>

#include "SDLHelpers.hpp"

class AnalogClock
{
public:
    AnalogClock(const RendererPtr &renderer);
    void Paint();
private:
    const RendererPtr &renderer;
    float radius;
    SDL_FPoint center;
};

#endif