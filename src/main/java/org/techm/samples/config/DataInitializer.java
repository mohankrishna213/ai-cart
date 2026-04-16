package org.techm.samples.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.techm.samples.entity.Categories;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Reviews;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.repository.CategoriesRepository;
import org.techm.samples.repository.ProductsRepository;
import org.techm.samples.repository.ReviewsRepository;
import org.techm.samples.repository.UserInfoRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ReviewsRepository reviewsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {

        // ==========================================
        // 1. INITIALIZE USERS (1 Admin, Multiple Customers)
        // ==========================================
        User admin = userInfoRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@productcatalog.com");
            admin.setPassword(passwordEncoder.encode("root"));
            admin.setRole(Role.ADMIN);
            userInfoRepository.save(admin);
            System.out.println("✅ Admin user created.");
        }

        List<User> customers = new ArrayList<>();

        // Original legacy customer
        User legacyCustomer = userInfoRepository.findByUsername("customer").orElse(null);
        if (legacyCustomer == null) {
            legacyCustomer = new User();
            legacyCustomer.setUsername("customer");
            legacyCustomer.setEmail("customer@gmail.com");
            legacyCustomer.setPassword(passwordEncoder.encode("customer"));
            legacyCustomer.setRole(Role.CUSTOMER);
            legacyCustomer = userInfoRepository.save(legacyCustomer);
        }
        customers.add(legacyCustomer);

        // Generate 11 additional distinct customers
        String[] customerNames = {"alice", "bob", "charlie", "diana", "ethan", "fiona", "george", "hannah", "ian", "julia", "kevin"};
        for (String name : customerNames) {
            User c = userInfoRepository.findByUsername(name).orElse(null);
            if (c == null) {
                c = new User();
                c.setUsername(name);
                c.setEmail(name + "@example.com");
                c.setPassword(passwordEncoder.encode("password123"));
                c.setRole(Role.CUSTOMER);
                c = userInfoRepository.save(c);
            }
            customers.add(c);
        }
        System.out.println("✅ Generated 12 unique customer accounts for reviews.");

        // ==========================================
        // 2. INITIALIZE CATEGORIES
        // ==========================================
        List<Categories> categories = Arrays.asList(
                createCategory("Clothing", "Premium apparel including T-Shirts, Hoodies, and Jackets for all seasons."),
                createCategory("Gadgets", "High-tech electronics, headphones, power banks, and VR headsets."),
                createCategory("Stationary", "Office and school supplies including premium books, diaries, and pens."),
                createCategory("DrinkWare", "Eco-friendly water bottles, thermal flasks, and ceramic mugs."),
                createCategory("Bags", "Durable laptop backpacks, travel trolleys, and stylish tote bags."),
                createCategory("Ergonomic", "Orthopedic supports, ergonomic chairs, and posture correction accessories."),
                createCategory("Fitness", "Gym equipment, yoga mats, and workout accessories for a healthy lifestyle."),
                createCategory("Home & Kitchen", "Smart kitchen appliances and beautiful home decor."),
                createCategory("Books", "Bestselling fiction and non-fiction books.")
        );
        System.out.println("✅ Categories Initialized");

        // ==========================================
        // 3. POPULATE PRODUCTS
        // Guard: only runs when the product table is completely empty.
        // ==========================================
        if (productsRepository.count() == 0) {
            System.out.println("⏳ Populating products. This may take a moment...");
            createProducts(categories);
            System.out.println("✅ All products successfully populated!");
        }

        // ==========================================
        // 4. POPULATE REVIEWS
        // BUG FIX: Reviews have their OWN guard, independent of the products guard.
        // Previously, reviews were inside the products guard which meant if products
        // already existed from a prior run, reviews were NEVER created.
        // ==========================================
        if (reviewsRepository.count() == 0) {
            System.out.println("⏳ Populating category-specific reviews...");
            addAllReviewsToProducts(customers);
            System.out.println("✅ All reviews successfully populated!");
        }
    }

    // ==========================================
    // CATEGORY-SPECIFIC REVIEW DATA
    // Each entry: { positiveTitles[], neutralTitles[], negativeTitles[],
    //               positiveContents[], neutralContents[], negativeContents[] }
    // ==========================================

    private String[][] getClothingReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Perfect Fit and Great Quality!", "Absolutely Love This Purchase!", "Premium Feel at a Great Price" },
                // [1] Neutral Titles
                { "Decent Clothing for the Price", "Average Quality, Nothing Special", "It's Okay But Expected More" },
                // [2] Negative Titles
                { "Fabric Shrunk After First Wash", "Stitching Came Apart Quickly", "Sizing Is Wildly Inconsistent" },
                // [3] Positive Contents
                {
                        "I was genuinely surprised by the quality of this clothing item. The fabric feels soft and premium against the skin, and the stitching is very neat and tight throughout. I've worn it multiple times now and it still looks brand new after washing. The fit is exactly as described in the size chart — no surprises. If you're on the fence, just buy it. You will not regret it.",
                        "Ordered this for a casual event and I could not have been happier. The material is breathable and does not cling uncomfortably in warm weather. The colours are vibrant and true to the product photos. It washed well with no fading or pilling. Several people asked me where I got it, which says it all. This brand has definitely earned a repeat customer in me.",
                        "The build quality of this piece of clothing is outstanding for the price range. The inner lining is smooth, the outer fabric is durable, and the overall construction feels like something you'd find at double the cost. I'm placing another order in a different colour. Highly recommended to anyone looking for stylish, reliable apparel."
                },
                // [4] Neutral Contents
                {
                        "It's an okay purchase overall. The fabric is decent but not exceptional — nothing that will turn heads. Sizing runs slightly small so I'd recommend going one size up. It gets the job done for everyday wear but don't expect it to last more than a season or two with regular use. For the price, it's acceptable but I wouldn't call it great value.",
                        "This clothing item is perfectly average. It fits as expected and looks fine in person, but the material feels a bit thin and lightweight. I worry it may not survive many wash cycles before fading. It serves its purpose for casual use, but I probably wouldn't recommend it to someone looking for something that lasts.",
                        "Mixed feelings on this one. The style is attractive and it photographs well, but up close the fabric feels rough and the print quality is mediocre. I've seen better at a similar price point from other brands. Use it for casual outings and you'll be satisfied, but don't expect premium quality."
                },
                // [5] Negative Contents
                {
                        "Extremely disappointed with this purchase. After just one gentle machine wash on a cold cycle, the garment shrank by nearly two sizes. The fabric also developed several small pills and the colour faded noticeably. The product description is completely misleading about the material quality. I would strongly advise against buying this unless you want to waste your money.",
                        "The stitching on the shoulders came apart after the third wear. I didn't even do anything rough — just normal daily movement. Clearly the seams are not reinforced at stress points. I contacted support and they were unhelpful. Save your money and buy from a brand that actually cares about quality control. This is genuinely a terrible product.",
                        "The sizing on this item is completely wrong. I ordered my usual size based on the chart provided, and it arrived looking two sizes too big in some areas and too tight in others. The cut is just bizarre. On top of that, the fabric has a strange synthetic smell that did not wash out. Cannot recommend at all."
                }
        };
    }

    private String[][] getGadgetsReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Best Tech Purchase I've Made!", "Incredible Performance, Zero Complaints", "Outstanding Build and Battery Life" },
                // [1] Neutral Titles
                { "Works As Expected, Nothing Wow", "Decent Gadget for the Price", "Okay but Has Some Limitations" },
                // [2] Negative Titles
                { "Stopped Working After One Week", "Very Disappointing Build Quality", "Not Worth the Price At All" },
                // [3] Positive Contents
                {
                        "This gadget completely exceeded my expectations. The build quality is premium — no cheap plastic, no wobbly parts. The performance is fast, responsive, and consistent. Battery life is genuinely impressive and lasts well beyond what is advertised. Setup took under five minutes and everything just worked out of the box. If you need a reliable piece of tech, stop hesitating and just order this.",
                        "Absolutely brilliant. The audio/visual quality is leagues ahead of anything else I've tried in this price bracket. The connectivity is rock solid with no drops or lag. It handles everything I throw at it without breaking a sweat. The packaging was thoughtful and the accessories included are actually useful. This is the kind of purchase that makes you wonder why you waited so long.",
                        "I've been using this gadget daily for over a month and it shows zero signs of wear. The ergonomics are well thought out and extended use is comfortable. The feature set is genuinely useful and not just marketing fluff. Firmware updates have already improved performance since launch. Highly recommended for anyone who wants a device that just works, day in and day out."
                },
                // [4] Neutral Contents
                {
                        "This is a solid mid-range product. It does what it says on the box without any drama. The build quality is acceptable — some areas feel a bit plasticky but nothing that affects function. Battery life is average; you'll need to charge it every day or two. It doesn't have any standout features but there are no obvious deal breakers either. A reliable workhorse for the casual user.",
                        "Decent gadget that performs adequately for everyday tasks. The setup was straightforward and the device connected without issues. However, the sound/display quality is only marginally better than a budget alternative, and the premium price difference is not fully justified. The manual is thin on detail. Acceptable for light use but power users will want something more capable.",
                        "I'm neither impressed nor disappointed by this product. It functions correctly and the build feels stable enough. My concern is longevity — some of the mechanical parts feel like they may not withstand two or three years of regular use. For the price, it's competitive but not exceptional. If it lasts a couple of years, I'll call it good value."
                },
                // [5] Negative Contents
                {
                        "This gadget stopped functioning properly after just seven days of normal use. The device started glitching, then refused to charge, and finally became completely unresponsive. Customer support was a nightmare — they asked me to send fifteen different photos and then delayed the resolution for weeks. For a product at this price, the quality control is simply unacceptable. Do not waste your money.",
                        "The build quality is appalling. Buttons feel mushy and unresponsive, the casing has already developed micro-cracks from regular handling, and one of the ports is already loose. The advertised features are either missing or barely functional. I have genuinely seen better quality from products at a third of the price. This is a case of flashy marketing hiding a deeply mediocre product.",
                        "I purchased this hoping it would replace my existing device. The battery life is a fraction of what is advertised — I barely get three hours on a full charge. The connectivity drops every twenty minutes and requires a restart to fix. The software crashes without warning. I've had nothing but frustration since unboxing. Avoid this product at all costs."
                }
        };
    }

    private String[][] getStationaryReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Absolutely Love Writing With This!", "Premium Quality – Perfect Gift", "Smooth Ink Flow, Great Feel" },
                // [1] Neutral Titles
                { "Does the Job for Everyday Use", "Acceptable Quality, Nothing Special", "Okay Stationery for the Price" },
                // [2] Negative Titles
                { "Ink Dried Out Within a Month", "Cheap Feel Despite the Price", "Skips and Blotches Constantly" },
                // [3] Positive Contents
                {
                        "This stationery item is an absolute delight to use every day. The ink flow is incredibly smooth with zero skipping or blotching, and the grip is ergonomically designed for long writing sessions without fatigue. The build quality feels premium and the product looks exactly like the photographs. I've been a stationery enthusiast for years and this is now my go-to recommendation for anyone who loves writing.",
                        "I bought this as a gift and the recipient was genuinely thrilled. The packaging was elegant and presentable right out of the box. The quality of the writing experience is superb — it glides across the page with a satisfying weight and precision. The ink is rich and doesn't bleed through standard notebook pages. An excellent choice for gifting or treating yourself to something special.",
                        "Outstanding product for the price. The build is solid and the design is sophisticated. Writing feels controlled and precise, which makes a real difference during note-taking or journaling. Refills are easy to find and reasonably priced. I've gone through three notebooks since buying this and the quality hasn't diminished at all. A reliable, beautiful piece of stationery."
                },
                // [4] Neutral Contents
                {
                        "This stationery product is functional and does what it promises. The writing experience is smooth enough for daily office use, though nothing extraordinary. The grip is comfortable but the overall build feels a touch lightweight. Ink flow is consistent for the most part, with occasional minor skipping on low-quality paper. It's a perfectly acceptable option if you're looking for something reliable without spending too much.",
                        "An average stationery purchase. It writes well on most paper types but the ink tends to dry out faster than expected if left uncapped for even a few minutes. The design is clean and professional looking. For the price it is neither great nor disappointing. Would suit someone who needs a basic, dependable writing instrument rather than something for an enthusiast.",
                        "Mixed impressions on this one. The aesthetic is lovely and it looks premium on a desk. The writing experience starts well but becomes inconsistent after the first week — occasional blobbing and uneven ink flow. Not sure if it's a quality control issue or a property of the ink formula. Decent enough for casual use but not the quality the price point suggests."
                },
                // [5] Negative Contents
                {
                        "Deeply disappointed with this purchase. The ink dried out completely within three weeks of regular use, even with the cap on. When I tried to replace the refill, I discovered it uses a proprietary size that is nearly impossible to find locally. The build, which looked premium in photos, feels alarmingly cheap in person — almost hollow. This is overpriced and I regret buying it.",
                        "Despite the sleek appearance, the writing quality is terrible. The ink skips every few words and leaves blotches on the page. It also stains your fingers if you hold it for more than a few minutes. For a product positioned as premium stationery, the performance is an embarrassment. I've had significantly better experiences with much cheaper brands. Not worth even a fraction of the asking price.",
                        "This pen set is all looks and no substance. The pens leaked inside the case, staining the velvet lining and a couple of the other pens. The ink quality is inconsistent — some pens write fine, others are practically unusable. The cap fits poorly and doesn't click securely. What should have been a thoughtful gift became an embarrassing situation. Avoid this entirely."
                }
        };
    }

    private String[][] getDrinkwareReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Keeps Coffee Hot All Morning!", "Best Bottle I've Ever Owned", "Outstanding Insulation, Highly Recommend" },
                // [1] Neutral Titles
                { "Decent Bottle, Average Insulation", "Keeps Temperature Okay-ish", "Functional But Nothing Special" },
                // [2] Negative Titles
                { "Started Leaking After One Week", "Terrible Insulation, Very Disappointing", "Rust Spots Appeared Inside" },
                // [3] Positive Contents
                {
                        "This drinkware item has completely transformed my morning routine. I fill it with hot coffee before leaving home and it is still pleasantly warm nine hours later — a claim I was skeptical about until I tested it myself. The build quality is exceptional; the lid seals with a satisfying click and has never leaked even in my bag. The design is elegant and ergonomic. An absolutely brilliant product that I've already gifted to two family members.",
                        "I've gone through many water bottles and thermal flasks over the years, and this is genuinely the best I've owned. The vacuum insulation technology is impressive — ice cubes were still partially intact after 18 hours in my bag during a summer day. The mouth is wide enough for ice cubes and easy cleaning. The finish doesn't scratch easily and still looks pristine after months of daily use. Worth every rupee.",
                        "The insulation performance of this bottle is remarkable. I use it for both hot and cold beverages and it consistently outperforms the stated temperature retention times. The material is food-grade and odour-free, even with strong beverages like green tea or coffee. The cleaning brush fits perfectly inside. I've dropped it twice and there's not a single dent. Fantastic build quality all round."
                },
                // [4] Neutral Contents
                {
                        "Adequate drinkware for everyday use. The insulation keeps drinks warm for about four to five hours, which is acceptable but falls short of the eight hours advertised. The lid mechanism works fine but requires a firm twist to ensure a proper seal. The design is clean and modern. Not the best thermal bottle on the market but decent enough at this price point for someone who doesn't need all-day temperature retention.",
                        "This is a functional bottle with average performance. It keeps cold drinks reasonably cool for a few hours and hot drinks warm for a similar duration. The lid clicks shut but I occasionally find small drips at the seal. The capacity is accurate to what is stated. Build quality feels solid but not premium. A decent everyday option if you aren't too fussy about perfect insulation.",
                        "Serves its basic purpose without any real issues. The design is practical and the grip is comfortable. Temperature retention is okay — not impressive enough to brag about but not so poor as to be a problem. I wish the opening were slightly wider for easier cleaning. Overall a middling product that is unlikely to wow you but won't let you down either."
                },
                // [5] Negative Contents
                {
                        "This bottle started leaking from the lid seal after just one week of use. I initially thought I hadn't closed it properly, but after multiple attempts with the same result, I realised the lid gasket is defective. My bag has been soaked twice. The insulation is also barely noticeable — hot drinks go lukewarm within an hour and a half. A complete waste of money and I'm seeking a refund.",
                        "The insulation on this product is virtually non-existent. I filled it with boiling hot tea and within two hours it was barely warm. I tried cold water with ice and all the ice melted within four hours — in an air-conditioned room. The lid mechanism is stiff and awkward to operate. This is a product that looks good in photos but performs terribly in reality. Deeply disappointing.",
                        "After only a few weeks of regular use, I noticed rust-coloured spots beginning to appear on the inside of the bottle near the base. I always hand-wash it and let it dry completely — I follow proper care instructions. This should absolutely not happen with stainless steel drinkware. The product description claims it is rust-proof, which is clearly false. This is a health concern and I would not recommend this to anyone."
                }
        };
    }

    private String[][] getBagsReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Incredibly Durable, Perfect for Travel!", "Best Bag I've Ever Owned", "Premium Build, Fits Everything" },
                // [1] Neutral Titles
                { "Decent Bag for the Price", "Good But Has Minor Flaws", "Functional, Nothing Extraordinary" },
                // [2] Negative Titles
                { "Zipper Broke Within the First Week", "Handle Snapped Off After Two Uses", "Quality Far Below Expectations" },
                // [3] Positive Contents
                {
                        "I've been using this bag for over three months now across multiple trips and it has held up beautifully. The zippers are smooth and robust, the stitching shows no signs of stress even under a fully packed load, and the material resists water splash without any issue. The compartmentalization is intelligent and well thought out — there's a dedicated space for everything. Easily the best bag I've owned at this price point.",
                        "This bag is a genuine workhorse. I use it daily as a laptop bag, commute bag, and occasional overnight travel bag, and it handles all three roles brilliantly. The shoulder padding is thick and comfortable even when the bag is heavy. The anti-theft back pocket is a brilliant touch. Several colleagues have asked about it. The design is understated and professional. Highly recommended without any reservation.",
                        "Outstanding quality from the very first inspection. The hardware — buckles, zippers, D-rings — all feel solid and properly finished. The interior lining is durable and easy to wipe clean. Fits my 15.6-inch laptop with room to spare. The bag maintains its shape well even when not fully packed. I've already ordered a second one in a different style as a gift. This brand delivers excellent value."
                },
                // [4] Neutral Contents
                {
                        "This bag is perfectly acceptable for everyday use. The main compartment fits a 14-inch laptop comfortably but a 15-inch might be a squeeze. The exterior fabric feels durable though the interior lining is a bit thin. The shoulder strap adjusts well but could benefit from more padding for heavy loads. Not a bag you'll brag about, but one that gets the job done reliably.",
                        "A solid everyday bag with a few minor shortcomings. The design is clean and modern. However, one of the inner organizer pockets is oddly placed, making it difficult to access quickly. The bottom of the bag could use reinforced feet to prevent wear when placed on rough surfaces. For the price, it is competitive and will serve most users well for casual and professional use.",
                        "Mixed feelings about this purchase. The external appearance is very attractive and the bag looks high quality at first glance. However, some of the stitching inside the main compartment is noticeably uneven and I can see it may fray with heavy use. The size is accurate and the zippers work smoothly. It's a decent bag but the interior finish quality should be better at this price."
                },
                // [5] Negative Contents
                {
                        "The main zipper on the front pocket snapped clean off after six days of light daily use. I wasn't forcing it — just opening and closing it normally while commuting. The zipper teeth separated completely from the tape. For a bag marketed as durable travel gear, this is an absolutely unacceptable failure point. I'm disappointed with both the product and the seller's slow response to my complaint. Do not buy this bag.",
                        "Both the top carry handles failed within the first two weeks. The stitching around the handle loops pulled clean through the material, leaving the handle dangling by a single thread. I use the bag normally — no extreme weights — so this is clearly a manufacturing defect. The overall build looks good superficially but the structural integrity is lacking. This bag is not worth the money and I strongly advise looking elsewhere.",
                        "The product photographs are extremely misleading. The bag in person looks significantly cheaper than advertised — the material is thin and crinkles easily, and the zippers feel flimsy. Several pockets described on the product page simply don't exist. The capacity is visually the same as a bag half the price. Deeply frustrated by this purchase and will be seeking a refund."
                }
        };
    }

    private String[][] getErgonomicReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Finally, My Back Pain is Gone!", "Game Changer for Work-From-Home", "Worth Every Rupee for Daily WFH Use" },
                // [1] Neutral Titles
                { "Provides Some Relief, Nothing Dramatic", "Decent Support for Casual Use", "Helps a Little, But Could Be Better" },
                // [2] Negative Titles
                { "Gave Me More Back Pain, Not Less", "Foam Went Flat After Two Weeks", "Doesn't Stay in Place at All" },
                // [3] Positive Contents
                {
                        "I've suffered from chronic lower back pain from years of desk work, and this ergonomic support has made a measurable difference in just two weeks. The memory foam contours perfectly to my spine and provides consistent pressure relief throughout long work sessions. It straps securely to my office chair and doesn't slip. I've noticed a dramatic reduction in the aches I used to feel by mid-afternoon. A must-have for anyone with a sedentary job.",
                        "This is one of the best investments I've made for my home office setup. After eight hours at my desk, I used to end the day with a stiff neck and aching shoulders. Since using this ergonomic product, those issues have practically disappeared. The build quality is excellent — it hasn't deformed or lost its shape after months of daily use. The adjustability is intuitive and took seconds to set up. Highly recommended for remote workers.",
                        "The engineering behind this product is genuinely impressive. The lumbar support zone applies exactly the right amount of pressure on the lumbar curve without creating uncomfortable pressure points. I tried several competing products before this one and nothing comes close. My physiotherapist even commented positively on my improved posture. If you work long hours at a desk, this is not a luxury — it's a necessity."
                },
                // [4] Neutral Contents
                {
                        "This product offers a noticeable but modest improvement to sitting comfort. The support is adequate for short to medium-length work sessions but begins to feel less effective after five or six hours of continuous use. The foam density is decent and it hasn't lost shape visibly. The adjustable strap works but can loosen slightly over time. It's a reasonable product but not the miracle solution some reviews suggest.",
                        "Decent ergonomic product that provides some relief but doesn't fully deliver on the premium positioning. The memory foam is responsive and soft initially, but I noticed some compression after the first week. It's better than sitting without any support, and for occasional desk users it will likely be sufficient. Those with serious posture issues may need a more specialized solution.",
                        "It works as a basic ergonomic aid. Setup is easy and it looks professional. The lumbar support angle is somewhat adjustable which helps personalize the fit. My concern is durability — the foam already feels slightly less firm after three weeks compared to when I received it. Not a bad product but the long-term value is uncertain. Buy it if you need a quick and affordable solution."
                },
                // [5] Negative Contents
                {
                        "This product actually made my back pain worse, not better. The lumbar support pad is positioned too high for my lower back, pushing into my mid-spine instead, which created a new source of discomfort. After two weeks of trial, I had to stop using it entirely. The strap that secures it to the chair is poorly designed and constantly slides down during normal sitting. This is not a well-engineered product and I'd recommend seeing a physiotherapist for actual guidance.",
                        "The memory foam in this product went almost completely flat after just two weeks of daily use, meaning it no longer provides any meaningful support. At the original firmness it was comfortable, but now it's like sitting against a thin piece of fabric. For a product priced in the ergonomic support category, this foam quality is simply not acceptable. I expected at least a year of effective use.",
                        "This support product does not stay in place at all. Within minutes of sitting down, it migrates upward or sideways despite the strap being cinched as tightly as possible. It requires constant readjustment throughout the day which is more distracting than just sitting without it. The velcro on the strap is also weak and loses its grip with use. A frustrating and ineffective purchase."
                }
        };
    }

    private String[][] getFitnessReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Best Home Gym Equipment I've Bought!", "Incredibly Durable – Survives Daily Workouts", "Transformed My Home Workout Routine" },
                // [1] Neutral Titles
                { "Good for Beginners, Average for Pros", "Decent Fitness Gear for Occasional Use", "Does the Job, Nothing Exceptional" },
                // [2] Negative Titles
                { "Tore After Just Two Sessions", "Poor Build Quality for a Fitness Product", "Not Suitable for Heavy or Regular Use" },
                // [3] Positive Contents
                {
                        "I've been using this fitness equipment daily for over two months and it shows zero signs of wear. The grip is firm and non-slip even with sweaty hands. The resistance and build quality are consistent with professional gym equipment — impressive for home use. It has made sticking to my workout routine so much easier by removing the need to visit the gym. An excellent investment in your health.",
                        "Outstanding quality. I was concerned about durability for a home product but it has held up beautifully under daily high-intensity use. The design is thoughtful — it's space-efficient when not in use and deploys quickly when needed. The instructions are clear and setup was simple. I've noticed genuine progress in my strength and flexibility over the past six weeks. Cannot recommend this highly enough.",
                        "This fitness product is exactly what my home workout was missing. The materials are top-quality — robust, well-finished, and comfortable to use for extended sessions. The weight distribution and ergonomics are clearly designed by someone who understands fitness biomechanics. It hasn't lost its shape, elasticity, or structural integrity despite regular intensive workouts. Well worth the investment."
                },
                // [4] Neutral Contents
                {
                        "A functional fitness product that covers the basics well. It's suitable for light to moderate workouts and beginners will find it more than adequate. Advanced or heavy users may find the resistance or durability lacking for very intensive sessions. The build quality is solid at first glance but some component materials feel slightly cheap. Decent value for someone starting their home fitness journey.",
                        "This gets the job done for occasional fitness sessions. It arrived in good condition and with all the parts accounted for. The resistance/weight is appropriate for its category. My main concern is how it holds up over 12+ months of regular use — some of the stress points look like they might be weak. For now it's performing well enough and I'm cautiously optimistic. Suitable for a home gym without demanding professional durability.",
                        "Adequate fitness equipment for the price. The performance is consistent and it integrates easily into a basic workout routine. However, the instructions are not very detailed and some assembly steps were ambiguous. The materials are functional but not premium-grade. If you're a casual fitness enthusiast looking for basic home workout gear without breaking the bank, this will serve you adequately."
                },
                // [5] Negative Contents
                {
                        "This fitness product tore along a seam after just two workout sessions of moderate intensity. I was not doing anything extreme — standard exercises well within the intended use case. The quality of the stitching and materials is clearly inadequate for the stress of regular physical activity. For a fitness product, this is a fundamental failure. I've asked for a replacement but haven't received a satisfactory response. Absolutely do not buy this.",
                        "Very disappointing build quality for a fitness product. The grip material started peeling after just a week of use and the structural components feel lightweight and fragile — nothing like the robust gym equipment it's photographed alongside. Under real workout loads, it flexes and creaks in ways that raise serious durability concerns. This is a product built for photography, not actual fitness use. Look for something more substantial.",
                        "Not suitable for anyone doing regular or heavy workouts. The product is fine for very light occasional use, but the moment you push it to moderate intensity, the quality issues become obvious. The resistance bands snapped after three sessions. The handles are slippery under exertion. The stated weight capacity is clearly optimistic. I've had to replace this within a month. Extremely poor value for money."
                }
        };
    }

    private String[][] getHomeKitchenReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Total Kitchen Game Changer!", "Best Appliance Purchase This Year", "Cooks Perfectly Every Single Time" },
                // [1] Neutral Titles
                { "Works Fine, Average Performance", "Decent Appliance for Daily Use", "Gets the Job Done, Nothing Wow" },
                // [2] Negative Titles
                { "Broke Down After Two Uses", "Very Poorly Made Appliance", "Absolutely Useless, Waste of Money" },
                // [3] Positive Contents
                {
                        "This kitchen appliance has genuinely changed the way I cook at home. The results are consistently excellent — whether I'm making quick weekday meals or elaborate weekend dishes, it delivers perfectly every time. The build quality is excellent — heavy, solid, and clearly designed for years of regular use. The settings are intuitive and the cleanup is remarkably easy. I've used it almost daily since buying it and I have zero complaints.",
                        "I was hesitant to spend this much on a kitchen appliance but it has been worth every rupee. The power and efficiency are noticeably superior to cheaper alternatives I've owned. It handles everything I've thrown at it with ease. The heat distribution is even and consistent, which has made a real difference to the quality of my cooking. The design is premium and it looks great on the counter. A brilliant purchase.",
                        "Outstanding performance from this appliance. It heats up quickly, maintains temperature accurately, and the controls are precise and easy to understand. Clean-up is simple — the surfaces are non-stick and food doesn't bake on. In three months of heavy use it hasn't given a single moment of trouble. This is one of those products that quietly becomes indispensable in your daily routine. Thoroughly recommended."
                },
                // [4] Neutral Contents
                {
                        "This kitchen appliance performs its core function reliably but doesn't stand out in any particular area. The build quality is acceptable for the price. Performance is consistent for standard tasks, though it struggles slightly with more demanding cooking scenarios. The control interface is functional but could be more intuitive. Cleaning is manageable but more time-consuming than premium alternatives. A solid everyday choice for a modest kitchen.",
                        "Decent appliance that gets the job done without any real drama. It heats up to the correct temperature and maintains it adequately. The capacity is accurate to what is described. My only complaint is that it is noisier than I expected during operation. The materials feel durable enough for moderate daily use. Not a product you'll get excited about, but one you can rely on for everyday cooking without surprises.",
                        "Average product that meets basic expectations. It functions correctly and the build seems solid enough for now. However, the non-stick coating shows minor scratches after just a few weeks of careful use with appropriate utensils. The included accessories are minimal — you'll need to buy extras. For the price it is reasonably competitive, though I've seen equivalent performance from cheaper brands."
                },
                // [5] Negative Contents
                {
                        "This appliance broke down completely after just two uses. On the second use, it made a loud popping noise and stopped functioning. The power light came on but there was no heat and no response from any of the controls. I followed the setup instructions exactly. Customer support was dismissive and asked me to ship the unit back at my own expense. For a product at this price, this level of failure and after-sales service is completely unacceptable.",
                        "The build quality of this appliance is genuinely shocking for the price. The exterior plastic casing feels thin and brittle, the control buttons are stiff and poorly responsive, and the seals around the cooking compartment look like they will fail with temperature cycling. I am genuinely concerned about using it long term. The performance also doesn't match the advertised specifications at all. This is a poorly engineered product dressed up with nice marketing.",
                        "An absolute waste of money. The non-stick coating started flaking into my food after just one week of gentle use with silicone utensils. Non-stick coatings should not degrade this rapidly — it suggests the coating is either very thin or of very poor quality. I would never risk consuming food cooked in this appliance again. I'm filing a consumer complaint. Avoid this product entirely."
                }
        };
    }

    private String[][] getBooksReviews() {
        return new String[][] {
                // [0] Positive Titles
                { "Absolutely Life-Changing Read!", "Cannot Put It Down – A Masterpiece", "One of the Best Books I've Ever Read" },
                // [1] Neutral Titles
                { "Decent Read With Some Good Points", "Worth a Read but Nothing New", "Some Insights, Quite Slow in Parts" },
                // [2] Negative Titles
                { "Very Overhyped, Deeply Disappointed", "Couldn't Get Past Halfway Through", "Dry Writing, Very Difficult to Finish" },
                // [3] Positive Contents
                {
                        "I picked up this book on a recommendation and it has quickly become one of the most impactful reads of my life. The author writes with remarkable clarity and backs every argument with compelling evidence and real-world examples. I found myself rereading entire sections because the insights were so dense and actionable. I've already applied several concepts from this book and noticed genuine changes. If you read one book this year, make it this one.",
                        "This is one of those rare books that manages to be both deeply educational and genuinely entertaining. The narrative flows beautifully and never becomes a slog, which is impressive given the complexity of the subject matter. I stayed up two nights running to finish it — something I rarely do. My copy is now full of highlighted passages and margin notes. A must-read for anyone interested in personal development, history, or simply great writing.",
                        "I came into this book as a sceptic and finished it as a complete convert. The author makes complex ideas genuinely accessible without dumbing them down or being patronizing. The structure is perfect — each chapter builds on the last and by the end everything comes together brilliantly. I've gifted this to three friends since finishing it. Brilliant, transformative, and exactly the kind of book the world needs more of right now."
                },
                // [4] Neutral Contents
                {
                        "This is a decent book with several genuinely interesting ideas, but it doesn't quite live up to its extraordinary reputation. The first half is engaging and fast-paced, but the second half becomes repetitive and padded with anecdotes that don't add much to the central argument. The core message could have been delivered in half the pages. Worth reading for the key insights, but temper your expectations relative to the hype.",
                        "Some interesting ideas here, but the execution is uneven. The writing style alternates between engaging and overly academic, making for an inconsistent reading experience. The examples and case studies are occasionally compelling but sometimes feel cherry-picked to support a predetermined conclusion. I'd recommend reading a detailed summary first to see if the full book is worth your time and money.",
                        "A middling read. The topic is fascinating and the first few chapters set up a promising exploration, but the author loses the thread and the book becomes increasingly scattered in the second half. There are good ideas buried in here but they require patience to uncover. Not a bad book, just not the transformative experience it's marketed as. Worth borrowing from a library before committing to buying."
                },
                // [5] Negative Contents
                {
                        "I finished this book out of a sense of obligation and deeply regret the time spent. The central thesis is stretched impossibly thin across nearly 400 pages, with the same point repeated in slightly different words every few chapters. The tone is condescending and assumes the reader is incapable of drawing their own conclusions. Despite its enormous commercial success, I found almost nothing of practical value. This book is the literary equivalent of empty calories.",
                        "I genuinely could not get past the halfway point of this book. The writing is dry, overly technical in places, and deeply boring in others. The author makes interesting promises in the introduction and then spends the rest of the book failing to deliver on them. I tried reading it in short sessions, long sessions, and with coffee — nothing helped. This is one of the most tedious reading experiences I've had in years.",
                        "Hugely overhyped. The ideas presented as groundbreaking are either obvious common sense or have been articulated far better in other books. The author's examples are repetitive and the writing style lacks any energy or passion. I was expecting depth and got surface-level observations dressed up in verbose prose. If you're looking for the insights promised by this book, you'll find better versions of them elsewhere for free."
                }
        };
    }

    // ==========================================
    // REVIEW ADDITION: queries existing products
    // and assigns category-relevant reviews
    // ==========================================

    private void addAllReviewsToProducts(List<User> customers) {
        List<Products> allProducts = productsRepository.findAll();
        if (allProducts.isEmpty()) {
            System.out.println("⚠️ No products found. Skipping review population.");
            return;
        }

        int totalReviewsAdded = 0;
        for (Products product : allProducts) {
            String categoryName = (product.getCategory() != null)
                    ? product.getCategory().getName()
                    : "General";

            // Random count between 1 and 10 (inclusive), never exceeding available customers
            int reviewCount = 1 + random.nextInt(Math.min(10, customers.size()));
            totalReviewsAdded += addReviews(product, reviewCount, customers, categoryName);
        }
        System.out.println("✅ Added " + totalReviewsAdded + " reviews across " + allProducts.size() + " products.");
    }

    /**
     * Adds category-relevant reviews to a product.
     * Guarantees each reviewer is unique per product by shuffling the customer list.
     * The title is always set (fixes the null-title bug).
     *
     * @return the number of reviews successfully saved
     */
    private int addReviews(Products product, int requestedCount, List<User> allCustomers, String categoryName) {
        if (product == null || requestedCount <= 0 || allCustomers == null || allCustomers.isEmpty()) return 0;

        int count = Math.min(requestedCount, allCustomers.size());

        // Shuffle to guarantee unique, random user selection per product
        List<User> shuffled = new ArrayList<>(allCustomers);
        Collections.shuffle(shuffled, random);

        // Fetch category-specific review data
        // [0]=positiveTitles [1]=neutralTitles [2]=negativeTitles
        // [3]=positiveContents [4]=neutralContents [5]=negativeContents
        String[][] reviewData = getCategoryReviewData(categoryName);

        int savedCount = 0;
        for (int i = 0; i < count; i++) {
            User reviewer = shuffled.get(i);

            Reviews review = new Reviews();
            review.setProduct(product);
            review.setUser(reviewer);

            int prob = random.nextInt(100);

            if (prob < 55) {
                // 55% → 5-star positive
                review.setRating(5.0);
                review.setTitle(reviewData[0][random.nextInt(reviewData[0].length)]);
                review.setContent(reviewData[3][random.nextInt(reviewData[3].length)]);
            } else if (prob < 75) {
                // 20% → 4-star positive
                review.setRating(4.0);
                review.setTitle(reviewData[0][random.nextInt(reviewData[0].length)]);
                review.setContent(reviewData[3][random.nextInt(reviewData[3].length)]);
            } else if (prob < 90) {
                // 15% → 3-star neutral
                review.setRating(3.0);
                review.setTitle(reviewData[1][random.nextInt(reviewData[1].length)]);
                review.setContent(reviewData[4][random.nextInt(reviewData[4].length)]);
            } else {
                // 10% → 1-2 star negative
                review.setRating((double) (random.nextInt(2) + 1));
                review.setTitle(reviewData[2][random.nextInt(reviewData[2].length)]);
                review.setContent(reviewData[5][random.nextInt(reviewData[5].length)]);
            }

            try {
                reviewsRepository.save(review);
                savedCount++;
            } catch (Exception e) {
                System.err.println("❌ Failed to save review for '" + product.getName()
                        + "' by user '" + reviewer.getUsername() + "': " + e.getMessage());
            }
        }
        return savedCount;
    }

    /**
     * Returns category-specific review content arrays.
     * Index layout: [0]=positiveTitles [1]=neutralTitles [2]=negativeTitles
     *               [3]=positiveContents [4]=neutralContents [5]=negativeContents
     */
    private String[][] getCategoryReviewData(String categoryName) {
        if (categoryName == null) return getGenericReviews();
        switch (categoryName) {
            case "Clothing":       return getClothingReviews();
            case "Gadgets":        return getGadgetsReviews();
            case "Stationary":     return getStationaryReviews();
            case "DrinkWare":      return getDrinkwareReviews();
            case "Bags":           return getBagsReviews();
            case "Ergonomic":      return getErgonomicReviews();
            case "Fitness":        return getFitnessReviews();
            case "Home & Kitchen": return getHomeKitchenReviews();
            case "Books":          return getBooksReviews();
            default:               return getGenericReviews();
        }
    }

    private String[][] getGenericReviews() {
        return new String[][] {
                { "Great Product!", "Very Happy With This Purchase", "Excellent Quality" },
                { "Decent for the Price", "Average Quality", "Gets the Job Done" },
                { "Very Disappointing", "Poor Quality", "Would Not Recommend" },
                {
                        "This product exceeded my expectations. The build quality is excellent and it performs flawlessly in daily use. Highly recommended to anyone looking for a reliable product.",
                        "Excellent purchase all round. Quick delivery, well packaged, and works exactly as described. Would not hesitate to buy from this seller again.",
                        "Very happy with this product. It is sturdy, well made, and functions perfectly. Great value for money and I will definitely recommend it."
                },
                {
                        "A fairly average product. Does what it says but there are no standout qualities that distinguish it from cheaper alternatives. Acceptable for everyday use.",
                        "It does the job without any major issues. The build is decent but nothing premium. Reasonable value for money if you don't need anything exceptional.",
                        "Mixed feelings. Some aspects are good, others disappoint. Overall it is passable for basic use but I expected more at this price point."
                },
                {
                        "Very disappointed with this purchase. The product did not perform as advertised and felt cheaply made. I would not recommend this to anyone.",
                        "Terrible experience. Product stopped working within days of delivery. Customer support was unhelpful and the refund process was a hassle.",
                        "The product is nothing like the photos or description. Poor quality materials, poor performance. A complete waste of money."
                }
        };
    }

    // ==========================================
    // PRODUCT CREATION (no reviews — separated)
    // ==========================================
    private void createProducts(List<Categories> categories) {

        Categories clothing  = categories.get(0);
        Categories gadgets   = categories.get(1);
        Categories stationary = categories.get(2);
        Categories drinkware = categories.get(3);
        Categories bags      = categories.get(4);
        Categories ergonomic = categories.get(5);
        Categories fitness   = categories.get(6);
        Categories home      = categories.get(7);
        Categories books     = categories.get(8);

        // ---- CLOTHING ----
        createProduct("Puma Mens ESS Tipping Polo T-Shirt", 1229.0,
                "Premium branded Puma Polo T-Shirt featuring a classic collar with tipping details. Made from a comfortable, highly breathable cotton blend. Ideal for casual outings, golf, or semi-formal events.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/puma-mens-ess-tipping-polo-t-shirt-54679775327129.jpg.webp?v=8787", clothing);
        createProduct("Hummel Jaye Polyester Polo T-Shirt", 799.0,
                "Anti-odor, quick-dry polyester material designed for intense workouts and sports. Features a lightweight design that wicks sweat away from your body keeping you cool all day.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/hummel-jaye-polyester-polo-t-shirt-77022653273257.jpg.webp?v=8787", clothing);
        createProduct("Jack and Jones Interlock Polo", 999.0,
                "Plain coloured, sophisticated polo shirt with a smooth interlock knit fabric. Offers a regular fit that drapes perfectly for a smart-casual office look.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/jack-and-jones-interlock-polo-56534721463520.jpg.webp?v=8787", clothing);
        createProduct("Mens Shirt", 899.0,
                "Cotton Rich formal men's shirt with full sleeves and a classic collar. Perfect for office wear and formal gatherings, providing both comfort and style.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/product/mens-shirt-14187958559778.jpg.webp?v=8787", clothing);
        createProduct("Womens Pink Shirts", 799.0,
                "Comfortable and stylish women's pink shirt made of soft cotton fabric. Features a relaxed fit suitable for daily wear, college, or casual outings.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/womens-pink-shirts-36050512377047.jpg.webp?v=8787", clothing);
        createProduct("Puma Caps", 609.0,
                "Stylish Puma branded cap with adjustable strap for a universal fit. Protects from the sun while adding a sporty touch to your outfit. Hand wash recommended.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/puma-caps-93491736013479.jpg.webp?v=8787", clothing);
        createProduct("High Neck Jacket", 1199.0,
                "Contemporary fit high-neck jacket. Windproof and lightly padded for exceptional warmth during the winter season. Includes two deep side pockets with zippers.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/high-neck-jacket-51517176358598.jpg.webp?v=8787", clothing);
        createProduct("Hoodies (Unisex)", 1699.0,
                "Cozy unisex hoodie featuring a full front zipper and adjustable drawstrings. Made from heavy fleece material to keep you warm during cold weather.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/hoodies-(unisex)-76265949253806.jpg.webp?v=8787", clothing);
        createProduct("Classic Denim Jacket", 1899.0,
                "Timeless blue denim jacket with button closures and chest pockets. A versatile wardrobe staple that pairs perfectly with t-shirts and casual outfits for a rugged look.",
                "https://dummyimage.com/400x400/2980b9/ffffff&text=Denim+Jacket", clothing);
        createProduct("Womens Workout Leggings", 899.0,
                "High-waisted, squat-proof workout leggings with a side pocket for your phone. Made from stretchy, moisture-wicking fabric for maximum mobility during heavy exercise.",
                "https://dummyimage.com/400x400/8e44ad/ffffff&text=Workout+Leggings", clothing);

        // ---- GADGETS ----
        createProduct("IRUSU Monster VR Headset with Remote Controller", 2199.0,
                "Immersive 3D virtual reality headset with fully adjustable HD lenses. Includes a Bluetooth remote controller for gaming. Compatible with all smartphones up to 6.5 inches.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/irusu-monster-vr-headset-with-remote-controller-19390348541456.jpg.webp?v=8787", gadgets);
        createProduct("Portronics headphones Muff M", 1999.0,
                "Over-ear wireless headphones with water-resistant materials. Features deep bass, noise isolation cushions, and up to 10 hours of uninterrupted playback time.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/portronics-headphones-muff-m-15897236099499.jpg.webp?v=8787", gadgets);
        createProduct("Mini Power Bank Encore+ 5000 mAh", 999.0,
                "Ultra-compact 5000mAh high capacity power bank. Easily fits in your pocket. Provides fast charging with dual USB output ports and LED battery indicators.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/mini-power-bank-encore-5000-mah-81361222522419.jpg.webp?v=8787", gadgets);
        createProduct("Google Nest Audio with Google Assistant", 1599.0,
                "Smart speaker with incredible room-filling stereo sound. Control your smart home, play music, and check the weather using just your voice with Google Assistant.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/google-nest-audio-with-google-assistant-58703816740552.jpg.webp?v=8787", gadgets);
        createProduct("Fitness Smartwatch Series 5", 2499.0,
                "Advanced smartwatch with heart rate monitoring, sleep tracking, and a pedometer. Features a vibrant AMOLED display and is water-resistant up to 50 meters.",
                "https://dummyimage.com/400x400/2c3e50/ffffff&text=Smartwatch", gadgets);
        createProduct("Ergonomic Wireless Mouse", 699.0,
                "Optical wireless mouse with a comfortable ergonomic grip. Connects via a 2.4GHz USB receiver. Features silent click buttons and adjustable DPI settings.",
                "https://dummyimage.com/400x400/34495e/ffffff&text=Wireless+Mouse", gadgets);
        createProduct("Mechanical Gaming Keyboard", 3299.0,
                "RGB backlit mechanical gaming keyboard with tactile blue switches. Designed for rapid response and durability during intense gaming sessions.",
                "https://dummyimage.com/400x400/1abc9c/ffffff&text=Gaming+Keyboard", gadgets);
        createProduct("Active Noise Cancelling Earbuds", 4599.0,
                "True wireless earbuds featuring advanced active noise cancellation (ANC). Deliver crystal clear audio and feature a charging case that extends battery life up to 24 hours.",
                "https://dummyimage.com/400x400/34495e/ffffff&text=ANC+Earbuds", gadgets);

        // ---- STATIONARY ----
        createProduct("Premium Notebook - Capri", 442.0,
                "High quality faux leather notebook with 200 thick, bleed-resistant ruled pages. Perfect for journaling, office notes, and executive gifting.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/premium-notebook-capri-46733133101458.jpg.webp?v=8787", stationary);
        createProduct("Custom Doodle Diary", 234.0,
                "Creative unruled doodle diary with a special women's day theme cover. Thick 120gsm paper allows sketching with markers without bleeding through the page.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/custom-doodle-diary-40723023343914.jpg.webp?v=8787", stationary);
        createProduct("Black Matt Watermark Pen Set", 737.0,
                "Elegant black matte finish pen set with high quality laser engraving. Provides a smooth writing experience. Comes in a beautiful velvet gift box.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/black-matt-watermark-pen-set-90336275167843.jpg.webp?v=8787", stationary);
        createProduct("Estilo Ball Pen Black", 209.0,
                "Professional smudge-free black ballpoint pen. Sleek design ensures a comfortable grip for long writing sessions. Ideal for students and professionals.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/estilo-ball-pen-black-94379510878107.jpg.webp?v=8787", stationary);
        createProduct("Pastel Highlighters Set of 6", 299.0,
                "Set of 6 aesthetic pastel colored highlighters. Chisel tip allows for both broad highlighting and fine underlining. Perfect for aesthetic note-taking.",
                "https://dummyimage.com/400x400/f1c40f/ffffff&text=Highlighters", stationary);
        createProduct("Luxury Fountain Pen", 1299.0,
                "Classic luxury fountain pen with an iridium medium nib for flawless ink flow. Includes a refillable ink converter and a premium presentation box.",
                "https://dummyimage.com/400x400/000000/ffffff&text=Fountain+Pen", stationary);

        // ---- DRINKWARE ----
        createProduct("Artiart Zebra Mug", 599.0,
                "Suction base 100% BPA free mug that prevents accidental spills on your desk. Features a fun zebra print and double-wall insulation to keep drinks warm.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/artiart-zebra-mug-21255075154276.jpg.webp?v=8787", drinkware);
        createProduct("Artiart Dumbo Mug", 699.0,
                "Cute Dumbo themed spill-proof mug with a 450 ml capacity. The innovative grip-pad design securely attaches to flat surfaces. Great for office desks.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/artiart-dumbo-mug-39384195983434.jpg.webp?v=8787", drinkware);
        createProduct("Servewell Osaka - SS Single Wall Bottle - 675 ml", 499.0,
                "Eco-friendly 675ml stainless steel single wall water bottle. Rust-proof, leak-proof, and lightweight. Perfect for gym, cycling, and daily hydration.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/servewell-osaka-ss-single-wall-bottle-675-ml-91086500592985.jpg.webp?v=8787", drinkware);
        createProduct("Stainless Steel Vacuum Bottle 300ml (Assorted Colours)", 999.0,
                "Double-wall vacuum insulated thermos bottle. Keeps beverages hot for 12 hours or cold for 24 hours. Features a secure thermos bottle cap and a compact 300ml size.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/stainless-steel-vacuum-bottle-300ml-(assorted-colours)-37290950800317.jpg.webp?v=8787", drinkware);
        createProduct("Pure Copper Water Bottle 1L", 899.0,
                "Ayurvedic pure copper water bottle with a leak-proof seal. Storing water in copper overnight is known to improve digestion and boost the immune system.",
                "https://dummyimage.com/400x400/d35400/ffffff&text=Copper+Bottle", drinkware);

        // ---- BAGS ----
        createProduct("Uppercase JFK Hard Luggage Trolley Bag Cabin", 3339.0,
                "Printed Hard-Sided polycarbonate cabin trolley bag. Features 8 spinner wheels for smooth rolling, a TSA-approved lock, and an anti-scratch surface.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/uppercase-jfk-hard-luggage-trolley-bag-cabin-29130103999941.jpg.webp?v=8787", bags);
        createProduct("Fuzo Gym Bag - Play", 938.0,
                "Ultra-lightweight duffel gym bag that is foldable into a compact pouch. Includes a separate shoe compartment and water-resistant lining for sweaty clothes.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/fuzo-gym-bag-play-95926730934339.jpg.webp?v=8787", bags);
        createProduct("Voyager Paris Valerie Sling Bag", 820.0,
                "Chic women's sling bag with adjustable PU leather straps. Perfect for carrying daily essentials with multiple zipper compartments and a stylish minimal design.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/voyager-paris-valerie-sling-bag-13137876646313.jpg.webp?v=8787", bags);
        createProduct("Voyager Paris Louis Laptop Backpack", 1681.0,
                "Premium professional laptop backpack fitting laptops up to 15.6 inches. Features detachable and adjustable PU Leather straps, water-resistant fabric, and organizer pockets.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/voyager-paris-louis-laptop-backpack-38522804838299.jpg.webp?v=8787", bags);
        createProduct("Rejean Boho Pink Tote Bag", 1268.0,
                "Large size boho-style pink canvas tote bag with sturdy handles. Eco-friendly and spacious enough for groceries, beach trips, or everyday casual use.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/rejean-boho-pink-tote-bag-41125111243689.jpg.webp?v=8787", bags);
        createProduct("Vintage Leather Duffle Bag", 4500.0,
                "Premium handcrafted genuine leather duffle bag. Ideal for weekend getaways and travel. Features heavy-duty brass hardware and a detachable shoulder strap.",
                "https://dummyimage.com/400x400/8b4513/ffffff&text=Leather+Duffle", bags);

        // ---- ERGONOMIC ----
        createProduct("PALO Orthopedic Long Back Support with Memory Foam", 1269.0,
                "Orthopedic long back support cushion made with high-density memory foam. Specifically designed for spine alignment and relieving lower back pain during long hours of sitting.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-orthopedic-long-back-support-with-memory-foam-80045758933650.jpg.webp?v=8787", ergonomic);
        createProduct("PALO Premium Memory Foam Travel Neck Pillow", 679.0,
                "U-shaped travel neck pillow with a washable velvet cover and premium memory foam cushion. Provides excellent 360-degree neck support for airplane and car travel.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-premium-memory-foam-travel-neck-pillow-89325638200288.jpg.webp?v=8787", ergonomic);
        createProduct("PALO Footrest", 1649.0,
                "Adjustable under-desk footrest made of High Impact Polystyrene. Helps improve posture and blood circulation by keeping feet and legs elevated while working.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-footrest-88333606986144.jpg.webp?v=8787", ergonomic);
        createProduct("PALO Premium Ergonomic Backrest with Memory Foam", 1269.0,
                "Lumbar support backrest featuring a secure adjustable strap to fit any office chair. Molded memory foam provides targeted relief to the lower spine.",
                "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-premium-ergonomic-backrest-with-memory-foam-42275449327201.jpg.webp?v=8787", ergonomic);
        createProduct("Adjustable Standing Desk Converter", 5499.0,
                "Easily convert your standard desk into a healthy sit-stand workstation. Features a smooth gas-spring height adjustment mechanism and a wide tray for dual monitors.",
                "https://dummyimage.com/400x400/2c3e50/ffffff&text=Standing+Desk", ergonomic);

        // ---- FITNESS ----
        createProduct("ProGrip Non-Slip Yoga Mat 6mm", 899.0,
                "High-density eco-friendly TPE yoga mat. 6mm thickness offers comfortable cushioning for joints. Features a double-sided non-slip texture for excellent traction.",
                "https://dummyimage.com/400x400/16a085/ffffff&text=Yoga+Mat", fitness);
        createProduct("Hex Adjustable Dumbbell Set 15kg", 3499.0,
                "Cast iron hex dumbbell set with an anti-roll design. Includes a textured steel handle for secure grip. Perfect for home gym strength training and muscle building.",
                "https://dummyimage.com/400x400/34495e/ffffff&text=Dumbbell+Set", fitness);
        createProduct("Heavy Duty Resistance Bands Set", 1299.0,
                "Set of 5 stackable resistance tube bands. Includes handles, ankle straps, and a door anchor. Provides up to 150 lbs of resistance for full-body home workouts.",
                "https://dummyimage.com/400x400/f39c12/ffffff&text=Resistance+Bands", fitness);
        createProduct("Cast Iron Kettlebell 8kg", 1899.0,
                "Solid cast iron kettlebell with a wide, comfortable grip handle. Flat bottom prevents rolling. Excellent for swings, deadlifts, squats, and functional training.",
                "https://dummyimage.com/400x400/2c3e50/ffffff&text=Kettlebell", fitness);

        // ---- HOME & KITCHEN ----
        createProduct("AeroBlend Pro Digital Blender", 4299.0,
                "1000W high-speed digital blender perfect for smoothies, ice crushing, and hot soups. Comes with a 1.5L BPA-free Tritan jar and 6 stainless steel blades.",
                "https://dummyimage.com/400x400/e74c3c/ffffff&text=Digital+Blender", home);
        createProduct("HealthyFry 5L Air Fryer", 5999.0,
                "Oil-free digital air fryer with 8 preset cooking modes. Cook crispy fries, chicken, and vegetables with 85% less fat. Features a non-stick, dishwasher-safe basket.",
                "https://dummyimage.com/400x400/c0392b/ffffff&text=Air+Fryer", home);
        createProduct("Pre-Seasoned Cast Iron Skillet 10-inch", 1499.0,
                "Heavy-duty cast iron skillet for exceptional heat retention and even cooking. Pre-seasoned with vegetable oil. Safe for stovetop, oven, and campfire cooking.",
                "https://dummyimage.com/400x400/000000/ffffff&text=Cast+Iron+Skillet", home);

        // ---- BOOKS ----
        createProduct("Clean Code by Robert C. Martin", 650.0,
                "A Handbook of Agile Software Craftsmanship. A must-read for any software developer aiming to write robust, maintainable, and clean code. Contains practical examples in Java.",
                "https://dummyimage.com/400x400/8e44ad/ffffff&text=Clean+Code+Book", books);
        createProduct("Atomic Habits by James Clear", 499.0,
                "An Easy & Proven Way to Build Good Habits & Break Bad Ones. This transformative book teaches you practical strategies to form good habits, break bad ones, and master tiny behaviors.",
                "https://dummyimage.com/400x400/f1c40f/ffffff&text=Atomic+Habits", books);
        createProduct("The Pragmatic Programmer", 850.0,
                "Your journey to mastery. This classic programming book covers topics ranging from personal responsibility and career development to architectural techniques for building resilient code.",
                "https://dummyimage.com/400x400/2980b9/ffffff&text=Pragmatic+Programmer", books);
        createProduct("The Alchemist by Paulo Coelho", 299.0,
                "A magical fable about following your dream. This enchanting story tells the magical journey of Santiago, an Andalusian shepherd boy who yearns to travel in search of a worldly treasure.",
                "https://dummyimage.com/400x400/d35400/ffffff&text=The+Alchemist", books);
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private Categories createCategory(String name, String description) {
        if (!categoriesRepository.existsByName(name)) {
            Categories cat = new Categories();
            cat.setName(name);
            cat.setDescription(description);
            return categoriesRepository.save(cat);
        }
        return categoriesRepository.findByName(name).orElse(null);
    }

    private Products createProduct(String name, double price, String description, String imageUrl, Categories category) {
        try {
            Products p = new Products();
            p.setName(name);
            p.setPrice(price);
            p.setDescription(description);
            p.setImageUrl(imageUrl);
            p.setStockQuantity(random.nextInt(146) + 5);
            p.setAvailable(true);
            p.setCategory(category);
            return productsRepository.save(p);
        } catch (Exception e) {
            System.err.println("❌ Failed to save product: " + name);
            e.printStackTrace();
            return null;
        }
    }
}