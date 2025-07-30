package org.techm.samples.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.techm.samples.entity.Categories;
import org.techm.samples.entity.Products;
import org.techm.samples.entity.Role;
import org.techm.samples.entity.User;
import org.techm.samples.repository.CategoriesRepository;
import org.techm.samples.repository.ProductsRepository;
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
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		
		if(!userInfoRepository.existsByUsername("admin")) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setEmail("admin@productcatalog.com");
			admin.setPassword(passwordEncoder.encode("root"));
			admin.setRole(Role.ADMIN);
			userInfoRepository.save(admin);
		}
		
		if(!userInfoRepository.existsByUsername("customer")) {
			User customer = new User();
			customer.setUsername("customer");
			customer.setEmail("customer@gmail.com");
			customer.setPassword(passwordEncoder.encode("customer"));
			customer.setRole(Role.CUSTOMER);
			userInfoRepository.save(customer);
		}
		
		List<Categories> categories=Arrays.asList(
			createCategory("Clothing","T-Shirts Hoodies and Jackets"),
			createCategory("Gadgets","Headphones power banks and VR"),
			createCategory("Stationary","Books and pens"),
			createCategory("DrinkWare","Bottles and mugs"),
			createCategory("Bags","Laptop and travel bags"),
			createCategory("ergonomic", "tables chairs and beds")
		);
		
		System.out.println(categories);
		if (productsRepository.count()==0) {
			createSimpleProducts(categories);
		}

	}
	

	private Categories createCategory(String name,String description) {
		if (!categoriesRepository.existsByName(name)) {
			Categories categories=new Categories();
			categories.setName(name);
			categories.setDescription(description);
			return categoriesRepository.save(categories);
		}
		return categoriesRepository.findByName(name).orElse(null);
	}

	private void createSimpleProducts(List<Categories> categories) {
		// Clothing (categoryId = 0)
		Categories clothing=categories.get(0);
		createProduct(
		    "Puma Mens ESS Tipping Polo T-Shirt",
		    (double)1229,
		    "Branded Puma T-Shirt",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/puma-mens-ess-tipping-polo-t-shirt-54679775327129.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "Hummel Jaye Polyester Polo T-Shirt",
		    (double)799,
		    "Anti-odor",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/hummel-jaye-polyester-polo-t-shirt-77022653273257.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "Jack and Jones Interlock Polo",
		    (double)999,
		    "Plain Coloured",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/jack-and-jones-interlock-polo-56534721463520.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "Mens Shirt",
		    (double)899,
		    "Cotton Rich",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/product/mens-shirt-14187958559778.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "Womens Pink Shirts",
		    (double)799,
		    "Comfortable",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/womens-pink-shirts-36050512377047.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "Puma Caps",
		    (double)609,
		    "Hand Wash",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/puma-caps-93491736013479.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "High Neck Jacket",
		    (double)1199,
		    "Contemporary fit",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/high-neck-jacket-51517176358598.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		createProduct(
		    "Hoodies (Unisex)",
		    (double)1699,
		    "full front zipper",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/hoodies-(unisex)-76265949253806.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    clothing
		);

		Categories gadgets=categories.get(1);
		createProduct(
		    "IRUSU Monster VR Headset with Remote Controller",
		    (double)2199,
		    "Fully Adjustable lens",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/irusu-monster-vr-headset-with-remote-controller-19390348541456.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    gadgets
		);

		createProduct(
		    "Portronics headphones Muff M",
		    (double)1999,
		    "Water Resistant Pack",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/portronics-headphones-muff-m-15897236099499.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    gadgets
		);

		createProduct(
		    "Mini Power Bank Encore+ 5000 mAh",
		    (double)999,
		    "High Capacity",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/mini-power-bank-encore-5000-mah-81361222522419.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    gadgets
		);

		createProduct(
		    "Google Nest Audio with Google Assistant",
		    (double)1599,
		    "Stereo sound",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/google-nest-audio-with-google-assistant-58703816740552.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    gadgets
		);

		// Drinkware (categoryId = 2)
		Categories drinkware=categories.get(3);
		createProduct(
		    "Artiart Zebra Mug",
		    (double)599,
		    "100% BPA free",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/artiart-zebra-mug-21255075154276.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    drinkware
		);

		createProduct(
		    "Artiart Dumbo Mug",
		    (double)699,
		    "450 ml capacity",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/artiart-dumbo-mug-39384195983434.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    drinkware
		);

		createProduct(
		    "Servewell Osaka - SS Single Wall Bottle - 675 ml",
		    (double)499,
		    "Eco-friendly",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/servewell-osaka-ss-single-wall-bottle-675-ml-91086500592985.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    drinkware
		);

		createProduct(
		    "Stainless Steel Vacuum Bottle 300ml (Assorted Colours)",
		    (double)999,
		    "Thermos bottle cap",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/stainless-steel-vacuum-bottle-300ml-(assorted-colours)-37290950800317.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    drinkware
		);
		
		// Stationary (categoryId = 3)
		Categories stationary=categories.get(2);
		createProduct(
		    "Premium Notebook - Capri",
		    (double)442,
		    "High quality faux leather",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/premium-notebook-capri-46733133101458.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    stationary
		);

		createProduct(
		    "Custom Doodle Diary",
		    (double)234,
		    "Special women's day theme",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/custom-doodle-diary-40723023343914.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    stationary
		);

		createProduct(
		    "Black Matt Watermark Pen Set",
		    (double)737,
		    "High quality Laser Engraving",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/black-matt-watermark-pen-set-90336275167843.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    stationary
		);

		createProduct(
		    "Estilo Ball Pen Black",
		    (double)209,
		    "Smudge-free pen",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/estilo-ball-pen-black-94379510878107.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    stationary
		);

		// Bags (categoryId = 4)
		Categories bags=categories.get(4);
		createProduct(
		    "Uppercase JFK Hard Luggage Trolley Bag Cabin",
		    (double)3339,
		    "Printed Hard - Sided Body",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/uppercase-jfk-hard-luggage-trolley-bag-cabin-29130103999941.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    bags
		);

		createProduct(
		    "Fuzo Gym Bag - Play",
		    (double)938,
		    "Foldable into a Pouch",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/fuzo-gym-bag-play-95926730934339.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    bags
		);

		createProduct(
		    "Voyager Paris Valerie Sling Bag",
		    (double)820,
		    "Adjustable PU leather straps",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/voyager-paris-valerie-sling-bag-13137876646313.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    bags
		);

		createProduct(
		    "Voyager Paris Louis Laptop Backpack",
		    (double)1681,
		    "Detachable and adjustable PU Leather straps",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/voyager-paris-louis-laptop-backpack-38522804838299.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    bags
		);

		createProduct(
		    "Rejean Boho Pink Tote Bag",
		    (double)1268,
		    "Large size and sturdy handles",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/rejean-boho-pink-tote-bag-41125111243689.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    bags
		);

		// Ergonomic (categoryId = 5)
		Categories ergonomic=categories.get(5);
		createProduct(
		    "PALO Orthopedic Long Back Support with Memory Foam",
		    (double)1269,
		    "Spine Alignment",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-orthopedic-long-back-support-with-memory-foam-80045758933650.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    ergonomic
		);

		createProduct(
		    "PALO Premium Memory Foam Travel Neck Pillow",
		    (double)679,
		    "Velvet Cover & Memory Foam Cushion",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-premium-memory-foam-travel-neck-pillow-89325638200288.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    ergonomic
		);

		createProduct(
		    "PALO Footrest",
		    (double)1649,
		    "Made of High Impact Polystyrene",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-footrest-88333606986144.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    ergonomic
		);

		createProduct(
		    "PALO Premium Ergonomic Backrest with Memory Foam",
		    (double) 1269,
		    "Adjustable Strap",
		    "https://deq64r0ss2hgl.cloudfront.net/images/opt/products_gallery_images/palo-premium-ergonomic-backrest-with-memory-foam-42275449327201.jpg.webp?v=8787",
		    Integer.valueOf(10),
		    true,
		    ergonomic
		);
	}
	private void createProduct(String name, double price, String description, String imageUrl, Integer stock_quantity, boolean available, Categories categories) {
	try {
		Products products=new Products();
		products.setName(name);
		products.setPrice(price);
		products.setDescription(description);
		products.setImageUrl(imageUrl);
		products.setStockQuantity(stock_quantity);
		products.setAvailable(available);
		products.setCategory(categories);
		productsRepository.save(products);
		System.out.println("✅ Saved product: " + name);
    } catch (Exception e) {
        System.err.println("❌ Failed to save product: " + name);
        e.printStackTrace();
    }
	}
	
}
