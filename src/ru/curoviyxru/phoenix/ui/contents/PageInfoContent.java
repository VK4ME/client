package ru.curoviyxru.phoenix.ui.contents;

import java.util.Enumeration;
import org.json.me.JSONArray;
import ru.curoviyxru.j2vk.PageStorage;
import ru.curoviyxru.j2vk.VKConstants;
import ru.curoviyxru.j2vk.api.objects.VKObject;
import ru.curoviyxru.j2vk.api.objects.user.Group;
import ru.curoviyxru.j2vk.api.objects.user.Page;
import ru.curoviyxru.j2vk.api.objects.user.User;
import ru.curoviyxru.phoenix.Localization;
import ru.curoviyxru.phoenix.midlet.Midlet;
import ru.curoviyxru.phoenix.ui.Content;
import ru.curoviyxru.phoenix.ui.Label;
import ru.curoviyxru.phoenix.ui.ListItem;

/**
 *
 * @author curoviyxru
 */
public class PageInfoContent extends Content {
    
    Page page;
    
    public PageInfoContent(Page p) {
        super(Localization.get("title.additionalInfo"));
        
        PageStorage.load(p.getId(), VKConstants.full_user_fields);
        page = PageStorage.get(p.getId());
        
        if (page != null) {
            setTitle("@" + page.getNickname());
            if (page.isGroup) loadGroupInfo();
            else loadUserInfo();
        }
    }
    
    public void loadGroupInfo() {
        final Group g = page.asGroup();
        
        if (!VKObject.isEmpty(g.activity)) {
            add(new Label(Localization.get("element.activity")).setFont(true));
            add(new Label(g.activity));
        }
        
        if (g.age_limits != 0) {
            add(new Label(Localization.get("element.age_limits")).setFont(true));
            add(new Label(Localization.get("ageLimits." + (g.age_limits == 2 ? "sixteen" : g.age_limits == 3 ? "eighteen" : "none"))));
        }
        
        if (!VKObject.isEmpty(g.description)) {
            add(new Label(Localization.get("element.description")).setFont(true));
            add(new Label(g.description));
        }
        
        if (!VKObject.isEmpty(g.site)) {
            add(new Label(Localization.get("element.site")).setFont(true));
            add(new ListItem(g.site, ListItem.GOTO) {
                public void actionPerformed() {
                    Midlet.goLink(g.site);
                }
            });
        }
        
        if (!VKObject.isEmpty(g.country_title)) {
            add(new Label(Localization.get("element.country_title")).setFont(true));
            add(new Label(g.country_title));
        }
        
        if (!VKObject.isEmpty(g.city_title)) {
            add(new Label(Localization.get("element.city_title")).setFont(true));
            add(new Label(g.city_title));
        }
    }
    
    public void loadUserInfo() {
        final User g = page.asUser();
        
        if (!VKObject.isEmpty(g.bdate)) {
            add(new Label(Localization.get("element.bdate")).setFont(true));
            add(new Label(g.bdate));
        }
        
        if (!VKObject.isEmpty(g.about)) {
            add(new Label(Localization.get("element.about")).setFont(true));
            add(new Label(g.about));
        }
        
        if (!VKObject.isEmpty(g.mobile_phone)) {
            add(new Label(Localization.get("element.mobile_phone")).setFont(true));
            add(new Label(g.mobile_phone));
        }
        
        if (!VKObject.isEmpty(g.home_phone)) {
            add(new Label(Localization.get("element.home_phone")).setFont(true));
            add(new Label(g.home_phone));
        }
        
        if (!VKObject.isEmpty(g.site)) {
            add(new Label(Localization.get("element.site")).setFont(true));
            add(new ListItem(g.site, ListItem.GOTO) {
                public void actionPerformed() {
                    Midlet.goLink(g.site);
                }
            });
        }
        
        if (!VKObject.isEmpty(g.occupation_name) && !VKObject.isEmpty(g.occupation_type)) {
            add(new Label(Localization.get("element.occupation")).setFont(true));
            add(new Label(Localization.get("occupation."+g.occupation_type)+", "+g.occupation_name));
        }
        
        String education = "";
        if (!VKObject.isEmpty(g.university_name)) {
            education += g.university_name;
        }
        if (!VKObject.isEmpty(g.faculty_name)) {
            if (education.length() != 0) education += ", ";
            education += g.faculty_name;
        }
        if (g.graduation != 0) {
            if (education.length() != 0) education += ", ";
            education += g.graduation;
        }
        if (education.length() != 0) {
            add(new Label(Localization.get("element.education")).setFont(true));
            add(new Label(education));
        }
        
        if (g.connections != null && g.connections.length() > 0) {
            add(new Label(Localization.get("element.connections")).setFont(true));
            String result = "";
            Enumeration e = g.connections.keys();
            while (e.hasMoreElements()) {
                String k = (String) e.nextElement();
                if (k == null) continue;
                String v = g.connections.optString(k);
                if (v == null) continue;
                
                result += k + ": " + v;
            }
            add(new Label(result));
        }
        
        if (g.relation != 0) {
            add(new Label(Localization.get("element.relation")).setFont(true));
            add(new Label(Localization.get("relation."+String.valueOf(g.relation))));
            if (g.relation_partner_id != 0)
                add(ContentController.getUserButton(PageStorage.get(g.relation_partner_id), false, false, false, false, this));
        }
        
        if (!VKObject.isEmpty(g.country_title)) {
            add(new Label(Localization.get("element.country_title")).setFont(true));
            add(new Label(g.country_title));
        }
        
        if (!VKObject.isEmpty(g.city_title)) {
            add(new Label(Localization.get("element.city_title")).setFont(true));
            add(new Label(g.city_title));
        }
        
        if (!VKObject.isEmpty(g.home_town)) {
            add(new Label(Localization.get("element.home_town")).setFont(true));
            add(new Label(g.home_town));
        }
        
        if (g.personal != null) {
            add(new Label(Localization.get("element.personal")).setFont(true));
            int political = g.personal.optInt("political");
            if (political != 0) {
                add(new Label(Localization.get("element.political")).setFont(true));
                add(new Label(Localization.get("political."+String.valueOf(political))));
            }
            JSONArray langs = g.personal.optJSONArray("langs");
            if (langs != null && langs.length() != 0) {
                String langsString = "";
                for (int i = 0; i < langs.length(); ++i) {
                    String lItem = langs.optString(i);
                    if (lItem == null) continue;
                    if (langsString.length() != 0) langsString += ", ";
                    langsString += lItem;
                }
                if (langsString.length() != 0) {
                    add(new Label(Localization.get("element.langs")).setFont(true));
                    add(new Label(langsString));
                }
            }
            String religion = g.personal.optString("religion");
            if (!VKObject.isEmpty(religion)) {
                add(new Label(Localization.get("element.religion")).setFont(true));
                add(new Label(religion));
            }
            String inspiredBy = g.personal.optString("inspired_by");
            if (!VKObject.isEmpty(inspiredBy)) {
                add(new Label(Localization.get("element.inspiredBy")).setFont(true));
                add(new Label(inspiredBy));
            }
            int people_main = g.personal.optInt("people_main");
            if (people_main != 0) {
                add(new Label(Localization.get("element.people_main")).setFont(true));
                add(new Label(Localization.get("people_main."+String.valueOf(people_main))));
            }
            int life_main = g.personal.optInt("life_main");
            if (life_main != 0) {
                add(new Label(Localization.get("element.life_main")).setFont(true));
                add(new Label(Localization.get("life_main."+String.valueOf(life_main))));
            }
            int smoking = g.personal.optInt("smoking");
            if (smoking != 0) {
                add(new Label(Localization.get("element.smoking")).setFont(true));
                add(new Label(Localization.get("attitude."+String.valueOf(smoking))));
            }
            int alcohol = g.personal.optInt("alcohol");
            if (alcohol != 0) {
                add(new Label(Localization.get("element.alcohol")).setFont(true));
                add(new Label(Localization.get("attitude."+String.valueOf(alcohol))));
            }
        }
        
        if (!VKObject.isEmpty(g.interests)) {
            add(new Label(Localization.get("element.interests")).setFont(true));
            add(new Label(g.interests));
        }
        
        if (!VKObject.isEmpty(g.activities)) {
            add(new Label(Localization.get("element.activities")).setFont(true));
            add(new Label(g.activities));
        }
        
        if (!VKObject.isEmpty(g.books)) {
            add(new Label(Localization.get("element.books")).setFont(true));
            add(new Label(g.books));
        }
        
        if (!VKObject.isEmpty(g.tv)) {
            add(new Label(Localization.get("element.tv")).setFont(true));
            add(new Label(g.tv));
        }
        
        if (!VKObject.isEmpty(g.movies)) {
            add(new Label(Localization.get("element.movies")).setFont(true));
            add(new Label(g.movies));
        }
        
        if (!VKObject.isEmpty(g.music)) {
            add(new Label(Localization.get("element.music")).setFont(true));
            add(new Label(g.music));
        }
        
        if (!VKObject.isEmpty(g.games)) {
            add(new Label(Localization.get("element.games")).setFont(true));
            add(new Label(g.games));
        }
        
        if (!VKObject.isEmpty(g.quotes)) {
            add(new Label(Localization.get("element.quotes")).setFont(true));
            add(new Label(g.quotes));
        }
    }
}
