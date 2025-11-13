#ifndef __ALERT_SCREEN_HPP__
#define __ALERT_SCREEN_HPP__

#include "Alert.hpp"
#include "SDLHelpers.hpp"

class AlertScreen
{
public:
    AlertScreen(const RendererPtr &renderer);
    void Paint(const Alert &alert);

private:
    const RendererPtr &renderer;
    Size size;
};

#endif