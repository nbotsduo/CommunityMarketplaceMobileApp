package com.example.communitymarketplace.marketplace

import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.Product
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.searchmarket_layout.view.*

class SearchCard(val product: Product) : Item<GroupieViewHolder>() {

    var productObject:Product?=null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        productObject=product
        viewHolder.itemView.txt_SearchProductName.text=product.productName
        viewHolder.itemView.txt_SearchProductPrice.text="Rm "+product.price.toString()
        Picasso.get().load(product.productImageUrl).into(viewHolder.itemView.img_SearchMarket)
    }

    override fun getLayout(): Int {
        return R.layout.searchmarket_layout
    }
}