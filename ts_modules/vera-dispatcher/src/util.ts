export const html = (
    strings: TemplateStringsArray,
    ...values: (string | number)[]
): string =>
    String.raw({ raw: strings }, ...values);
