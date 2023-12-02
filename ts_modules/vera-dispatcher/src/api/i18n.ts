export type Locale = "fi_FI" | "sv_FI" | "smn_FI" | "sme_FI" | "sms_FI";

export type MultilingualString = {
    [Property in Locale]: string
}
